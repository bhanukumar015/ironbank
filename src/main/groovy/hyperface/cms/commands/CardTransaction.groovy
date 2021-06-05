package hyperface.cms.commands

import hyperface.cms.Constants
import hyperface.cms.Constants.LedgerEntryType
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry

class CardTransaction {
    String id
    Date transactedOn
    String retrievalReferenceNumber
    String description
    Double amount
    Constants.Currency currency
    Boolean postedToLedger
    enum TransactionType { Credit, Debit }
    TransactionType transactionType
    String mcc

    public CardTransaction(LedgerEntry ledgerEntry) {
        this.id = "ldg_" + ledgerEntry.id
        this.transactedOn = ledgerEntry.customerTxn ? ledgerEntry.customerTxn.createdOn : ledgerEntry.createdOn
        this.retrievalReferenceNumber = ledgerEntry.customerTxn?.retrievalReferenceNumber
        this.description = ledgerEntry.description ?: ledgerEntry.merchantName
        this.amount = ledgerEntry.amount
        this.currency = ledgerEntry.account.defaultCurrency
        this.postedToLedger = true
        this.transactionType = ledgerEntry.ledgerEntryType == LedgerEntryType.Debit ? TransactionType.Debit : TransactionType.Credit
    }

    public CardTransaction(CustomerTxn customerTxn) {
        this.id = "cst_" + customerTxn.id
        this.transactedOn = customerTxn.createdOn
        this.retrievalReferenceNumber = customerTxn.retrievalReferenceNumber
        this.postedToLedger = false
        this.description = customerTxn.merchantName
        this.amount = customerTxn.billingAmount
        this.currency = customerTxn.card.creditAccount.defaultCurrency
        this.postedToLedger = false
        this.transactionType = customerTxn.txnType == Constants.TxnType.AUTH ? TransactionType.Debit : TransactionType.Credit
    }
}
