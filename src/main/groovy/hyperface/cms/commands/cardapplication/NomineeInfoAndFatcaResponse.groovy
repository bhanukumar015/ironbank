package hyperface.cms.commands.cardapplication

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class NomineeInfoAndFatcaResponse {
    enum FatcaStatus {
        SUCCESS,
        FAILED
    }

    FatcaStatus status
    String errorMessage
    String applicationRefId
    String fixedDepositRefId
}
