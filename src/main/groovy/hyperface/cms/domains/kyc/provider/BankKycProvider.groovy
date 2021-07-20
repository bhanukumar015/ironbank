package hyperface.cms.domains.kyc.provider

import javax.persistence.Entity


/**
 * Entity for storing details when a BANK acts as KYC provider.
 */

@Entity
class BankKycProvider extends KycProvider {

    BankKycProvider() {
        this.setProviderType(ProviderType.BANK)
    }

    String bankName

}
