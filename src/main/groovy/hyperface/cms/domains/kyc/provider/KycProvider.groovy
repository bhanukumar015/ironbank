package hyperface.cms.domains.kyc.provider

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType

/**
 * Base entity for defining various KYC providers,
 * e.g., BANK, External PARTNER, etc.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
class KycProvider {
    enum ProviderType {
        BANK,
        PARTNER
    }

    @Id
    @GenericGenerator(name = "provider_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "provider_id")
    String id

    @Enumerated(EnumType.STRING)
    ProviderType providerType

    String providerStatus

}
