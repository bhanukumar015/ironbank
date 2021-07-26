package hyperface.cms.commands.cardapplication

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class CardEligibilityResponse {
    enum EligibilityStatus {
        APPROVED,
        PENDING_MOBILE_VERIFICATION,
        REJECTED
    }
    EligibilityStatus status
    String failureReason
    String applicationRefId
}
