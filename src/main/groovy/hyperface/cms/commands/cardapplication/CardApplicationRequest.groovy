package hyperface.cms.commands.cardapplication

import hyperface.cms.Constants
import hyperface.cms.domains.cardapplication.KycProof
import hyperface.cms.util.validation.StringEnumeration
import org.springframework.format.annotation.DateTimeFormat

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

class CardApplicationRequest {
    @NotBlank(message = "applicationRefId must not be null/empty")
    String applicationRefId
    @NotNull(message = "applicantDetails must not be null/empty")
    ApplicantDetails applicantDetails
    BankDetails bankDetails
    @NotBlank(message = "ipAddress must not be null/empty")
    String ipAddress
}

class ApplicantDetails {
    @StringEnumeration(enumClass = Constants.Salutation.class, message = "salutation must not be null/empty. Must be one of [Mr, Miss, Mrs].")
    String salutation
    @NotBlank(message = "firstName must not be null/empty")
    String firstName
    String middleName
    @NotBlank(message = "lastName must not be null/empty")
    String lastName
    @NotBlank(message = "nameOnCard must not be null/empty")
    String nameOnCard
    @StringEnumeration(enumClass = Constants.Gender.class, message = "gender must not be null/empty. Must be one of [MALE, FEMALE, OTHER].")
    String gender
    @NotBlank(message = "educationalQualification must not be null/empty")
    String educationalQualification
    @StringEnumeration(enumClass = Constants.MaritalStatus.class, message = "maritalStatus must not be null/empty. Must be one of [SINGLE, MARRIED, WIDOWED, SEPARATED, DIVORCED].")
    String maritalStatus
    String phoneNumber
    @NotBlank(message = "emailId must not be null/empty")
    @Email(message = "Invalid email ID")
    String emailId
    @NotNull(message = "permanentAddress must not be null/empty")
    CardApplicantAddress permanentAddress
    @NotNull(message = "residentialAddress must not be null/empty")
    CardApplicantAddress residentialAddress
    String employerName
}

class BankDetails {
    String bankAccountNumber
    String clientRelationshipNumber
    String clientPartnerRefId
}

class CardApplicantAddress {
    @NotBlank(message = "Address line1 must not be null/empty")
    String line1
    @NotBlank(message = "Address line2 must not be null/empty")
    String line2
    String line3
    @NotBlank(message = "Address city must not be null/empty")
    String city
    @NotBlank(message = "Address line1 must not be null/empty")
    @Pattern(regexp = "^[1-9][0-9]{5}\$", message = "Invalid pincode")
    String pincode
    @NotBlank(message = "state in address must not be null/empty")
    String state
    @NotBlank(message = "country in address must not be null/empty")
    String country
    String landmark
    @NotBlank(message = "country in address must not be null/empty")
    @Pattern(regexp = "^[A-Z]{2}\$", message = "Invalid country code")
    String countryCodeIso
}
