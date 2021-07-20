package hyperface.cms.domains

import hyperface.cms.model.enums.AuthorizationType
import hyperface.cms.model.enums.OnUsOffUsIndicator
import hyperface.cms.model.enums.SovereigntyIndicator
import hyperface.cms.model.enums.TransactionSourceIndicator
import hyperface.cms.model.enums.TransactionStatus

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity
class CustomerTransaction extends Transaction {

    CustomerTransaction() {
        this.setTxnSourceIndicator(TransactionSourceIndicator.CUSTOMER_INITIATED)
        this.setSovereigntyIndicator(SovereigntyIndicator.DOMESTIC)
    }

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
    String switchReferenceId

    @Enumerated(EnumType.STRING)
    AuthorizationType authorizationType

    @Enumerated(EnumType.STRING)
    OnUsOffUsIndicator onusOffusIndicator

    @Enumerated(EnumType.STRING)
    SovereigntyIndicator sovereigntyIndicator

    @Enumerated(EnumType.STRING)
    TransactionStatus txnStatus
}
