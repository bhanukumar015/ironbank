package hyperface.cms.commands.cardapplication

import hyperface.cms.Constants
import hyperface.cms.util.validation.PositiveValue
import hyperface.cms.util.validation.StringEnumeration
import org.springframework.format.annotation.DateTimeFormat

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

class CardEligibilityRequest {
    @NotBlank(message = "clientId must not be null/empty")
    String clientId
    @NotBlank(message = "programId must not be null/empty")
    String programId
    @NotBlank(message = "currentResidencePincode must not be null/empty")
    @Pattern(regexp = "^[1-9][0-9]{5}\$", message = "Invalid currentResidencePincode")
    String currentResidencePincode
    @StringEnumeration(enumClass = Constants.Profession.class, message = "profession must not be null/empty. Must be one of [SALARIED, SELF_EMPLOYED, OTHER].")
    String profession
    @NotBlank(message = "dob must not be null/empty")
    @DateTimeFormat(pattern = "yyyyMMdd")
    String dob
    @PositiveValue(message = "grossAnnualIncome must be an integer, greater than 0")
    Integer grossAnnualIncome
    @NotBlank(message = "mobileNumber must not be null/empty")
    @Pattern(regexp = "^[789]\\d{9}\$", message = "Invalid mobile number")
    String mobileNumber
    @NotBlank(message = "mobileNumberCountryCode must not be null/empty")
    @Pattern(regexp = "^[A-Z]{2}\$", message = "Invalid mobileNumberCountryCode")
    String mobileNumberCountryCode
    @NotBlank(message = "panNumber must not be null/empty")
    @Pattern(regexp = "^[A-Z0-9]{10}\$", message = "Invalid PAN number")
    String panNumber
    @NotBlank(message = "nationality must not be null/empty")
    String nationality
    @NotNull(message = "isMobileNumberVerified must not be null/empty")
    Boolean isMobileNumberVerified
}
