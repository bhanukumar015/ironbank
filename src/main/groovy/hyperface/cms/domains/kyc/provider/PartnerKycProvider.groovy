package hyperface.cms.domains.kyc.provider

import javax.persistence.Entity

/**
 * Entity for storing details when a BANK acts as KYC provider.
 */

@Entity
class PartnerKycProvider extends KycProvider {

    PartnerKycProvider() {
        this.setProviderType(ProviderType.PARTNER)
    }

    String partnerName

}
