package hyperface.cms.commands.cardapplication

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

class CustomerBankVerificationRequest {
    @NotBlank(message = "applicationRefId must not be null/empty")
    String applicationRefId

    @NotBlank(message = "accountNumber must not be null/empty")
    @Pattern(regexp = "^[0-9]*\$", message = "Invalid accountNumber")
    String accountNumber

    @NotBlank(message = "ifsCode must not be null/empty")
    @Pattern(regexp = "^[A-Z 0-9]*\$", message = "Invalid IFSC code")
    String ifsCode
}
