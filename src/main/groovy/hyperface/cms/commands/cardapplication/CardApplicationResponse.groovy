package hyperface.cms.commands.cardapplication

import com.fasterxml.jackson.annotation.JsonInclude
import hyperface.cms.domains.kyc.KycOption

@JsonInclude(JsonInclude.Include.NON_NULL)
class CardApplicationResponse {
    enum CardApplicationStatus {
        COMPLETE,
        PENDING,
        FAILED
    }
    CardApplicationStatus status
    String failureReason
    String applicationRefId
    String hyperfaceCustomerId
    KycOption.KycType kycMethod
    String bankCustomerId
}
