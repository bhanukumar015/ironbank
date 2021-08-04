package hyperface.cms.commands

import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.util.validation.PositiveValue

import javax.validation.constraints.NotBlank

class CreateCreditAccountRequest {
    @NotBlank(message = "Customer id should not be blank/null")
    String customerId
    @NotBlank(message = "Credit card program id should not be blank/null")
    String cardProgramId
    @PositiveValue(message = "Approved Credit Limit should be a double, greater than 0")
    Integer approvedCreditLimit

    // derived fields
    Customer customer
    CreditCardProgram cardProgram
}
