package hyperface.cms.domains.kyc.method

import hyperface.cms.domains.kyc.KycOption
import hyperface.cms.domains.kyc.provider.KycProvider
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
class KycMethod {
    @Id
    @GenericGenerator(name = "kyc_method_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "kyc_method_id")
    String id

    @Enumerated(EnumType.STRING)
    KycOption.KycType kycType

    String kycState //TODO: Need to come up with an enum when we have complete information

    @OneToOne
    @JoinColumn(name = "kyc_provider_id", referencedColumnName = "id")
    KycProvider kycProvider
}
