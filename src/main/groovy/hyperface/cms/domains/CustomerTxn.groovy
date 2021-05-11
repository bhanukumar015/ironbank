package hyperface.cms.domains

import hyperface.cms.Constants

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

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

}