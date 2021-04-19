package hyperface.cms.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

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
    Double amount
    String tid
    String mid
    String mcc
    String description

    // derived parameters
    PaymentInstrument paymentInstrument

}