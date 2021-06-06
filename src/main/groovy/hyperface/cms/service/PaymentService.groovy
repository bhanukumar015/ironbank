package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.Constants.LedgerEntryType
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.SettlementRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.interest.InterestCriteria
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerTxnRepository
import hyperface.cms.repository.LedgerEntryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class PaymentService {

    Logger log = LoggerFactory.getLogger(PaymentService.class);

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
        txn.cardSwitch = req.cardSwitch
        txn.switchTransactionId = req.transactionId
        txn.card = req.card
        txn.account = req.card.creditAccount
        txn.mid = req.merchantTerminalId
        txn.tid = req.merchantTerminalId
        txn.merchantName = req.merchantNameLocation
        txn.description = ""
        txn.retrievalReferenceNumber = req.retrievalReferenceNumber
        txn.systemTraceAuditNumber = req.systemTraceAuditNumber
        txn.txnRefId = req.transactionId
        txn.channel = CustomerTxn.Channel.Chip_And_Pin
        txn.txnType = req.transactionType
        txn.transactionCurrency = req.transactionCurrency
        txn.transactionAmount = req.transactionAmount
        txn.billingAmount = req.billingAmount
        txn.billingCurrency = req.billingCurrency
        txn.mcc = req.merchantCategoryCode
        if(txn.txnType == Constants.TxnType.AUTH) {
            txn.authorizedAmount = req.billingAmount
        }
        txn.transactedOn = req.transactionDate ?: new Date()
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
        log.info "Txn created for incoming authorization request: " + txn.dump()
        creditAccountRepository.save(req.card.creditAccount)
        return txn
    }

    // think about merging this with processAuthorization
    public CustomerTxn processReversalRequest(AuthorizationRequest req) {
        CustomerTxn txn = createCustomerTxn(req)
        req.card.creditAccount.availableCreditLimit += txn.billingAmount
        txn.availableBalanceAfterTxn = req.card.creditAccount.availableCreditLimit
        customerTxnRepository.save(txn)
        creditAccountRepository.save(req.card.creditAccount)
        return txn
    }

    // TODO - how to make sure that this is processed only once?
    public void processSettlementDebit(SettlementRequest req) {
        Card card = cardRepository.findById(req.cardId).get()
        assert card != null
        CreditAccount creditAccount = card.creditAccount

        CustomerTxn txn = customerTxnRepository.findAuthTxnByCardAndRRN(card, req.retrievalReferenceNumber)
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
        creditAccount.currentBalance -= ledgerEntry.amount
        txn.capturedAmount += req.settlementAmount
        // set txntype
        if(txn.txnType == Constants.TxnType.SETTLE_DEBIT) {
            ledgerEntry.txnType = Constants.TxnType.PURCHASE
        }
        else if(txn.txnType == Constants.TxnType.SETTLE_DEBIT_CASH) {
            ledgerEntry.txnType = Constants.TxnType.CASH_WITHDRAWAL
        }
        ledgerEntryRepository.save(ledgerEntry)
        creditAccountRepository.save(creditAccount)
        customerTxnRepository.save(txn)
    }

    public void processSettlementCredit(SettlementRequest req) {
        Card card = cardRepository.findById(req.cardId).get()
        assert card != null
        CreditAccount creditAccount = card.creditAccount
        CustomerTxn txn = customerTxnRepository.findAuthTxnByCardAndRRN(card, req.retrievalReferenceNumber)
        LedgerEntry ledgerEntry = createCreditEntry(creditAccount, txn, req.settlementAmount)
        creditAccount.currentBalance += ledgerEntry.amount
        if(txn.txnType == Constants.TxnType.SETTLE_CREDIT) {
            ledgerEntry.txnType = Constants.TxnType.PURCHASE_REVERSAL
        }
        else if(txn.txnType == Constants.TxnType.SETTLE_CREDIT_CASH) {
            ledgerEntry.txnType = Constants.TxnType.CASH_WITHDRAWAL_REVERSAL
        }
        ledgerEntryRepository.save(ledgerEntry)
        creditAccountRepository.save(creditAccount)
        customerTxnRepository.save(txn)
    }

    private LedgerEntry createDebitEntry(CreditAccount account, CustomerTxn customerTxn) {
        LedgerEntry debitEntry = new LedgerEntry()
        debitEntry.account = account
        debitEntry.amount = customerTxn.billingAmount
        debitEntry.createdOn = new Date()
        debitEntry.openingBalance = account.currentBalance
        debitEntry.ledgerEntryType = LedgerEntryType.Debit
        debitEntry.customerTxn = customerTxn
        debitEntry.description = customerTxn.description
        debitEntry.closingBalance = account.currentBalance - customerTxn.billingAmount
        return debitEntry
    }

    private LedgerEntry createDebitEntry(CreditAccount account, CustomerTxn customerTxn, Double settlementAmount) {
        LedgerEntry debitEntry = new LedgerEntry()
        debitEntry.account = account
        debitEntry.amount = settlementAmount
        debitEntry.createdOn = new Date()
        debitEntry.openingBalance = account.currentBalance
        debitEntry.ledgerEntryType = LedgerEntryType.Debit
        debitEntry.customerTxn = customerTxn
        debitEntry.merchantName = customerTxn.merchantName
        debitEntry.description = customerTxn.description
        debitEntry.closingBalance = account.currentBalance - settlementAmount
        return debitEntry
    }

    private LedgerEntry createCreditEntry(CreditAccount account, CustomerTxn customerTxn, Double settlementAmount) {
        LedgerEntry creditEntry = new LedgerEntry()
        creditEntry.account = account
        creditEntry.amount = settlementAmount
        creditEntry.openingBalance = account.currentBalance
        creditEntry.closingBalance = account.currentBalance + settlementAmount
        creditEntry.ledgerEntryType = LedgerEntryType.Credit
        creditEntry.description = customerTxn.description
        creditEntry.customerTxn = customerTxn
        creditEntry.merchantName = customerTxn.merchantName
        creditEntry.description = customerTxn.description
        creditEntry.createdOn = new Date()
        return creditEntry
    }

    private LedgerEntry createCreditEntry(CreditAccount account, CustomerTxn customerTxn) {
        LedgerEntry creditEntry = new LedgerEntry()
        creditEntry.account = account
        creditEntry.amount = customerTxn.billingAmount
        creditEntry.openingBalance = account.currentBalance
        creditEntry.closingBalance = account.currentBalance + customerTxn.billingAmount
        creditEntry.ledgerEntryType = LedgerEntryType.Credit
        creditEntry.description = customerTxn.description
        creditEntry.customerTxn = customerTxn
        creditEntry.createdOn = new Date()
        return creditEntry
    }

    public Integer getInterestRateForTxn(LedgerEntry ledgerEntry) {
        CreditCardProgram program = ledgerEntry.customerTxn.card.cardProgram
        InterestCriteria matchedCriteria = program.scheduleOfCharges.interestCriteriaList?.find({
            return it.checkForMatch(ledgerEntry)
        })
        return matchedCriteria?.getAprInBps()?:program.annualizedPercentageRateInBps
    }
}
