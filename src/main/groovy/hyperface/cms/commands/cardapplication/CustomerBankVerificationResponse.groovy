package hyperface.cms.commands.cardapplication

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomerBankVerificationResponse {
    enum VerificationStatus {
        SUCCESS,
        FAILED
    }

    VerificationStatus status
    String errorMessage
    String applicationRefId
    Double minCreditLineFdPercentage
    Double maxCreditLineFdPercentage
}
