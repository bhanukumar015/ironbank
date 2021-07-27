package hyperface.cms.domains.cardapplication

import hyperface.cms.Constants
import hyperface.cms.domains.Address
import hyperface.cms.domains.converters.AddressJsonConverter
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class DemographicDetail {
    @Id
    @GenericGenerator(name = "demographic_detail_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "demographic_detail_id")
    String id

    @Enumerated(EnumType.STRING)
    Constants.Salutation salutation

    String firstName
    String middleName
    String lastName
    String nameOnCard
    String dob
    Integer grossAnnualIncome
    String nationality

    @Enumerated(EnumType.STRING)
    Constants.Gender gender

    String educationalQualification

    @Enumerated(EnumType.STRING)
    Constants.MaritalStatus maritalStatus

    String phoneNumber
    String mobileNumber
    String countryCode
    String emailId

    @Convert(converter = AddressJsonConverter.class)
    Address permanentAddress

    @Convert(converter = AddressJsonConverter.class)
    Address residentialAddress

    @Enumerated(EnumType.STRING)
    Constants.Profession profession

    String employerName
}
