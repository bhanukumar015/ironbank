package hyperface.cms.domains.kyc

import hyperface.cms.domains.cardapplication.KycProof
import hyperface.cms.domains.kyc.method.KycMethod
import org.hibernate.annotations.GenericGenerator

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import java.time.ZonedDateTime

/**
 * Entity to store details of customers who have started or completed a KYC.
 */

@Entity
class CustomerKycDetail {
    enum KycStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
    @Id
    @GenericGenerator(name = "cust_kyc_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "cust_kyc_id")
    String id

    String hyperfaceCustomerId
    String bankCustomerId
    String clientCustomerId
    ZonedDateTime kycStartedOn
    ZonedDateTime nextStepInvocationOn
    ZonedDateTime kycCompletionOn

    @Enumerated(EnumType.STRING)
    KycStatus kycStatus

    @OneToOne
    @JoinColumn(name = "kyc_method_id", referencedColumnName = "id")
    KycMethod kycMethod

    @OneToMany(cascade = CascadeType.ALL)
    List<KycProof> kycProofs
}
