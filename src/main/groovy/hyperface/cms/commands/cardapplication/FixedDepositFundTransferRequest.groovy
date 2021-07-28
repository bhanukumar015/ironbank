package hyperface.cms.commands.cardapplication

import hyperface.cms.util.validation.PositiveValue

import javax.validation.constraints.NotBlank

class FixedDepositFundTransferRequest {
    @NotBlank(message = "applicationRefId must not be null/empty")
    String applicationRefId

    @PositiveValue(message = "fixedDepositAmount must be a number, greater than 0")
    Double fixedDepositAmount

}
