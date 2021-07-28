package hyperface.cms.commands.cardapplication

class FdBookingResponse {
    enum FdBookingStatus {
        SUCCESS,
        FAILED
    }

    FdBookingStatus status
    String errorMessage
    String fdAccountNumber
    String applicationRefId
    String fdRefId
}
