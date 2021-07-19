package hyperface.cms.domains.kyc.method

import javax.persistence.Entity
import java.time.ZonedDateTime

@Entity
class IpvPaperKycMethod extends KycMethod {
    String requiredDocUrl
    ZonedDateTime appointmentDate
}
