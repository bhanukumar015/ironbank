package hyperface.cms.commands

import hyperface.cms.Constants
import hyperface.cms.domains.Address
import hyperface.cms.util.validation.StringEnumeration

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

class CreateCustomerRequest {
    @NotBlank(message = "First name cannot be blank/null")
    String firstname
    String middlename
    @NotBlank(message = "Last name cannot be blank/null")
    String lastname
    @NotBlank(message = "PAN card number cannot be blank/null")
    String pancard
    @NotBlank(message = "First name cannot be blank/null")
    @Email(message = "Invalid email ID")
    String emailAddress
    @NotBlank(message = "Mobile number cannot be blank/null")
    String mobileNumber
    @NotBlank(message = "Mobile country code cannot be blank/null")
    String mobileCountryCode
    @NotBlank(message = "Date of birth cannot be blank/null")
    String dateOfBirth
    @NotBlank(message = "Country code cannot be blank/null")
    @Pattern(regexp = "^[A-Za-z]{2}\$", message = "Invalid value. Must be 2-letter ISO ISO Aplha-2 country code")
    String countryCode
    @NotBlank(message = "Nationality cannot be blank/null")
    @Pattern(regexp = "^[A-Za-z]{2}\$", message = "Invalid value. Must be 2-letter ISO ISO Aplha-2 country code")
    String nationality
    @StringEnumeration(enumClass = Constants.Gender.class, message = "Gender must not be null/empty. Must be one of [MALE, FEMALE, OTHER].")
    String gender
    Address currentAddress
    Address permanentAddress
}
