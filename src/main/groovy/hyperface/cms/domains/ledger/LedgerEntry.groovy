package hyperface.cms.domains.ledger

import hyperface.cms.Constants
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTxn

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    @ManyToOne
    CustomerTxn customerTxn

    Double openingBalance
    Double closingBalance

    @ManyToOne
    CreditAccount account

    Double amount
    @Enumerated(EnumType.STRING)
    Constants.LedgerEntryType ledgerEntryType
    Constants.TxnType txnType
    Date createdOn
    String merchantName
    String description
    String merchantCategoryCode
    String reasonCode
    String note

    String schemeFileComments
}
