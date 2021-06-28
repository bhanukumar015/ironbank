package hyperface.cms.domains

import hyperface.cms.model.enums.TransactionSourceIndicator
import hyperface.cms.model.enums.TransactionType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import java.time.ZonedDateTime

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
class Transaction {
    @Id
    @GenericGenerator(name = "txn_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "txn_id")
    String id

    String accountNumber
    String cardId
    ZonedDateTime txnDate
    ZonedDateTime txnPostingDate
    Double transactionAmount
    String transactionCurrency
    String rootReferenceId
    String txnDescription

    @CreationTimestamp
    ZonedDateTime createdOn

    @Enumerated(EnumType.STRING)
    TransactionType transactionType

    @Enumerated(EnumType.STRING)
    TransactionSourceIndicator txnSourceIndicator
}
