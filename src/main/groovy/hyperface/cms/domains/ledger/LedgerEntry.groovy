package hyperface.cms.domains.ledger

import hyperface.cms.Constants
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTxn

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    CustomerTxn customerTxn
    Double openingBalance
    Double closingBalance
    CreditAccount account
    Double amount
    Constants.LedgerEntryType ledgerEntryType
    Date createdOn
    String description
    String reasonCode
    String note
}
