package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.SettlementDebitRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerTxnRepository
import hyperface.cms.repository.LedgerEntryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class PaymentService {

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository

    @Autowired
    private CreditAccountRepository creditAccountRepository

    @Autowired
    CardRepository cardRepository

    @Autowired
    private CustomerTxnRepository customerTxnRepository

    private CustomerTxn createCustomerTxn(AuthorizationRequest req) {
        CustomerTxn txn = new CustomerTxn()
        txn.card = req.card
        txn.mid = req.merchantTerminalId
        txn.tid = req.merchantTerminalId
        txn.merchantName = req.merchantNameLocation
        txn.description = ""
        txn.retrievalReferenceNumber = req.retrievalReferenceNumber
        txn.systemTraceAuditNumber = req.systemTraceAuditNumber
        txn.txnRefId = req.transactionId
        txn.channel = CustomerTxn.Channel.Chip_And_Pin
        txn.txnType = CustomerTxn.TxnType.Authorize
        txn.transactionCurrency = req.transactionCurrency
        txn.transactionAmount = req.transactionAmount
        txn.billingAmount = req.billingAmount
        txn.billingCurrency = req.billingCurrency
        txn.mcc = req.merchantCategoryCode
        txn.authorizedAmount = req.billingAmount
        txn.postedToLedger = false
        return txn
    }

    public CustomerTxn processAuthorization(AuthorizationRequest req) {
        CustomerTxn txn = createCustomerTxn(req)
        // reduce this balance from the available credit limit
        // same currency
        req.card.creditAccount.availableCreditLimit -= txn.billingAmount
        txn.availableBalanceAfterTxn = req.card.creditAccount.availableCreditLimit
        customerTxnRepository.save(txn)
        creditAccountRepository.save(req.card.creditAccount)

        return txn
    }

    // TODO - how to make sure that this is processed only once?
    public void processSettlementDebit(SettlementDebitRequest req) {
        Card card = cardRepository.findById(req.cardId).get()
        CreditAccount creditAccount = card.creditAccount
        CustomerTxn txn = customerTxnRepository.findByCardAndRRN(card, req.retrievalReferenceNumber)
        // an approximate defense
        if(txn.capturedAmount + req.settlementAmount > 1.2 * txn.authorizedAmount) {
            throw new IllegalArgumentException("This entry looks to have been processed already")
        }
        txn.postedToLedger = true
        LedgerEntry ledgerEntry = createDebitEntry(creditAccount, txn, req.settlementAmount)
        if(txn.capturedAmount < txn.authorizedAmount && (txn.capturedAmount + req.settlementAmount) > txn.authorizedAmount) {
            creditAccount.availableCreditLimit -= ((txn.capturedAmount + req.settlementAmount) - txn.authorizedAmount)
        }
        else if(txn.capturedAmount >= txn.authorizedAmount) {
            creditAccount.availableCreditLimit -= req.settlementAmount
        }
        txn.capturedAmount += req.settlementAmount
        ledgerEntryRepository.save(ledgerEntry)
        creditAccountRepository.save(creditAccount)
        customerTxnRepository.save(txn)
    }

    public void processAuthCapture(CustomerTxn customerTxn) {
        Card card = (Card) customerTxn.card
        CreditAccount creditAccount = card.getCreditAccount()
        LedgerEntry debitEntry = createDebitEntry(creditAccount, customerTxn)
        creditAccount.availableCreditLimit -= customerTxn.billingAmount
        ledgerEntryRepository.save(debitEntry)
        creditAccountRepository.save(creditAccount)
        return
    }

    public void processRepayment(CustomerTxn customerTxn) {
        Card card = (Card) customerTxn.paymentInstrument
        CreditAccount creditAccount = card.getCreditAccount()
        LedgerEntry creditEntry = recordRepaymentTxn(creditAccount, customerTxn)
        ledgerEntryRepository.save(creditEntry)
        return
    }

    public LedgerEntry createDebitEntry(CreditAccount account, CustomerTxn customerTxn) {
        LedgerEntry debitEntry = new LedgerEntry()
        debitEntry.account = account
        debitEntry.amount = customerTxn.billingAmount
        debitEntry.createdOn = new Date()
        debitEntry.openingBalance = account.currentBalance
        debitEntry.ledgerEntryType = Constants.LedgerEntryType.Debit
        debitEntry.customerTxn = customerTxn
        debitEntry.description = customerTxn.description
        debitEntry.closingBalance = account.currentBalance - customerTxn.billingAmount
        return debitEntry
    }

    public LedgerEntry createDebitEntry(CreditAccount account, CustomerTxn customerTxn, Double settlementAmount) {
        LedgerEntry debitEntry = new LedgerEntry()
        debitEntry.account = account
        debitEntry.amount = settlementAmount
        debitEntry.createdOn = new Date()
        debitEntry.openingBalance = account.currentBalance
        debitEntry.ledgerEntryType = Constants.LedgerEntryType.Debit
        debitEntry.customerTxn = customerTxn
        debitEntry.merchantName = customerTxn.merchantName
        debitEntry.description = customerTxn.description
        debitEntry.closingBalance = account.currentBalance - settlementAmount
        return debitEntry
    }

    public LedgerEntry createCreditEntry(CreditAccount account, CustomerTxn customerTxn) {
        LedgerEntry creditEntry = new LedgerEntry()
        creditEntry.account = account
        creditEntry.amount = customerTxn.amount
        creditEntry.openingBalance = account.currentBalance
        creditEntry.closingBalance = account.currentBalance + customerTxn.amount
        creditEntry.ledgerEntryType = Constants.LedgerEntryType.Credit
        creditEntry.description = customerTxn.description
        creditEntry.customerTxn = customerTxn
        creditEntry.createdOn = new Date()
        return creditEntry
    }

    public LedgerEntry recordRepaymentTxn(CreditAccount account, CustomerTxn customerTxn) {
        if (customerTxn.txnType == CustomerTxn.TxnType.Repayment) {
            return null
        }

        return createCreditEntry(account, customerTxn)
    }

    public LedgerEntry recordRefundTxn(CreditAccount account, CustomerTxn customerTxn) {
        return createCreditEntry(account, customerTxn)
    }

    public LedgerEntry recordCashbackTxn(CreditAccount account, CustomerTxn customerTxn) {
        return null
    }
}
