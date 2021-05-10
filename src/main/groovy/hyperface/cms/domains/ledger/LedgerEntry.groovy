package hyperface.cms.domains.ledger

import hyperface.cms.Constants
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTxn

import javax.persistence.Entity
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
    Constants.LedgerEntryType ledgerEntryType
    Date createdOn
    String description
    String reasonCode
    String note
}
