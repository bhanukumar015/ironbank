package hyperface.cms.commands.cardapplication

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
