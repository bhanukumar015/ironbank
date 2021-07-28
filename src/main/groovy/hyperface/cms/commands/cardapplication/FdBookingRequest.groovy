package hyperface.cms.commands.cardapplication

import hyperface.cms.util.validation.PositiveValue

import javax.validation.constraints.NotBlank

class FdBookingRequest {
    @NotBlank(message = "applicationRefId must not be null/empty")
    String applicationRefId

    @NotBlank(message = "fdRefId must not be null/empty")
    String fdRefId

    @PositiveValue(message = "creditLimit must be a number, greater than 0")
    Double creditLimit

    String cardAccountNumber
}
