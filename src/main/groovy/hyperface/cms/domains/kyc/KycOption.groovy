package hyperface.cms.domains.kyc

import hyperface.cms.domains.CreditCardProgram
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

/**
 * An entity to register KYC options and details, which are
 * available for a card program.
 */

@Entity
class KycOption {
    enum KycType {
        IPV_PAPER("In Person Verification with paper based KYC"),
        IPV_BIOMETRIC("In- Person Aadhaar based biometric KYC"),
        eKYC_AADHAAR("Aadhaar Based e-KYC with OTP"),
        cKYC("CerSAI based KYC"),
        vKYC("Video KYC")

        private String description

        KycType(String description) {
            this.description = description
        }

        String getDescription() {
            return this.description
        }
    }

    @Id
    @GenericGenerator(name = "kyc_option_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "kyc_option_id")
    String id

    @Enumerated(EnumType.STRING)
    KycType kycType

    Integer priority
    Integer numberOfRetriesAllowed

    @ManyToOne(optional = false)
    @JoinColumn(name = "card_program_id", referencedColumnName = "id")
    CreditCardProgram creditCardProgram

}
