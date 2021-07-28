package hyperface.cms.commands.cardapplication

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class FixedDepositFundTransferResponse {
    enum TransferStatus {
        SUCCESS,
        FAILED
    }

    TransferStatus status
    String errorMessage
    String applicationRefId
    String transactionRefId
    Double amountTransferred
    String fdRefId
}
