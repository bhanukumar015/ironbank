package hyperface.cms.domains

import hyperface.cms.Constants
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.UpdateTimestamp

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class CustomerTxn {
    @Id
    @GenericGenerator(name = "customer_txn_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "customer_txn_id")
    String id

    enum TxnType {
        Authorize, Refund
    }
    enum Channel { MagStripe, Chip_And_Pin, NFC, Online }

    // incoming parameters
    boolean fullyAuthenticated = false
    @Enumerated(EnumType.STRING)
    Constants.CardSwitch cardSwitch

    String switchTransactionId

    @Enumerated(EnumType.STRING)
    TxnType txnType

    @Enumerated(EnumType.STRING)
    Channel channel

    String txnRefId
    Double transactionAmount = 0
    String transactionCurrency
    Double billingAmount = 0
    String billingCurrency
    String tid
    String mid
    String mcc
    String description
    String merchantName
    String retrievalReferenceNumber
    String systemTraceAuditNumber

    Double authorizedAmount = 0
    Double capturedAmount = 0

    boolean postedToLedger // even if a single transaction is posted for this Authorization

    Double availableBalanceAfterTxn

    @ManyToOne
    Card card

    @CreationTimestamp
    Date createdOn

    @UpdateTimestamp
    Date updatedOn

}