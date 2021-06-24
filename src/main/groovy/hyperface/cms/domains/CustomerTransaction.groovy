package hyperface.cms.domains

import hyperface.cms.model.enums.AuthorizationType
import hyperface.cms.model.enums.OnUsOffUsIndicator
import hyperface.cms.model.enums.SovereigntyIndicator
import hyperface.cms.model.enums.TransactionStatus
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class CustomerTransaction extends Transaction {
    @Id
    @GenericGenerator(name = "cust_txn_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "cust_txn_id")
    String id

    String cardHash
    Double pendingTxnAmount
    String authCode
    String tid
    String mid
    String mcc
    String schemeReferenceId
    String merchantCountryCode
    Double billingAmount
    String billingCurrency
    String posEntryMode

    @Enumerated(EnumType.STRING)
    AuthorizationType authorizationType

    @Enumerated(EnumType.STRING)
    OnUsOffUsIndicator onUsOffUsIndicator

    @Enumerated(EnumType.STRING)
    SovereigntyIndicator sovereigntyIndicator

    @Enumerated(EnumType.STRING)
    TransactionStatus txnStatus
}
