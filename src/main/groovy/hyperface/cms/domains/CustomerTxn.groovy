package hyperface.cms.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class CustomerTxn {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    enum TxnType { Repayment, Authorize, Capture, Auth_Capture, Refund }
    enum Channel { MagStripe, Chip_And_Pin, NFC, Online }

    // incoming parameters
    boolean fullyAuthenticated = false
    TxnType txnType
    Channel channel
    String txnRefId
    Double transactionAmount
    String transactionCurrency
    Double billingAmount
    String billingCurrency
    String tid
    String mid
    String mcc
    String description
    String merchantName
    String retrievalReferenceNumber
    String systemTraceAuditNumber

    Double authorizedAmount

    boolean postedToLedger // even if a single transaction is posted for this Authorization

    Double availableBalanceAfterTxn

    @ManyToOne
    Card card

}