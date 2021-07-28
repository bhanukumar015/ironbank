package hyperface.cms.commands.cardapplication

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class FdBookingResponse {
    enum FdBookingStatus {
        SUCCESS,
        FAILED
    }

    FdBookingStatus status
    String errorMessage
    String fixedDepositAccountNumber
    String applicationRefId
    String fixedDepositRefId
}
