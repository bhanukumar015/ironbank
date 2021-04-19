package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.LedgerEntryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PaymentService {

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository

    @Autowired
    private CreditAccountRepository creditAccountRepository

    public void processAuthCapture(CustomerTxn customerTxn) {
        Card card = (Card) customerTxn.paymentInstrument
        CreditAccount creditAccount = card.getCreditAccount()
        LedgerEntry debitEntry = createDebitEntry(creditAccount, customerTxn)
        creditAccount.availableCreditLimit -= customerTxn.amount
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
        debitEntry.amount = customerTxn.amount
        debitEntry.createdOn = new Date()
        debitEntry.openingBalance = account.currentBalance
        debitEntry.ledgerEntryType = Constants.LedgerEntryType.Debit
        debitEntry.customerTxn = customerTxn
        debitEntry.description = customerTxn.description
        debitEntry.closingBalance = account.currentBalance + customerTxn.amount
        return debitEntry
    }

    public LedgerEntry createCreditEntry(CreditAccount account, CustomerTxn customerTxn) {
        LedgerEntry creditEntry = new LedgerEntry()
        creditEntry.account = account
        creditEntry.amount = customerTxn.amount
        creditEntry.openingBalance = account.currentBalance
        creditEntry.closingBalance = account.currentBalance - customerTxn.amount
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
