package hyperface.cms.domains.ledger

import hyperface.cms.domains.Account
import hyperface.cms.domains.CreditAccount

import hyperface.cms.domains.Transaction
import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.OnUsOffUsIndicator
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import java.time.ZonedDateTime

@Entity
class TransactionLedger {
    @Id
    @GenericGenerator(name = "ledger_entry_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "ledger_entry_id")
    String id

    String accountNumber
    ZonedDateTime postingDate
    Double openingBalance
    Double closingBalance
    Double transactionAmount

    @Enumerated(EnumType.STRING)
    MoneyMovementIndicator moneyMovementIndicator

    @Enumerated(EnumType.STRING)
    LedgerTransactionType transactionType

    @Enumerated(EnumType.STRING)
    OnUsOffUsIndicator onusOffusIndicator

    @Enumerated(EnumType.STRING)
    FeeType feeType

    @ManyToOne
    @JoinColumn(name = "txn_ref_id", referencedColumnName = "id")
    Transaction transaction

    @ManyToOne
    CreditAccount creditAccount

    String txnDescription

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account
}
