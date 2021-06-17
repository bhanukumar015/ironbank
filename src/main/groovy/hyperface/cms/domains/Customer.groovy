package hyperface.cms.domains

import hyperface.cms.domains.converters.SimpleJsonConverter
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class Customer {
    @Id
    @GenericGenerator(name = "customer_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "customer_id")
    String id

    String firstName
    String middleName
    String lastName
    String preferredName

    String dateOfBirth
    String email
    String mobile
    String countryCode
    String nationality

    @Convert(converter = SimpleJsonConverter.class)
    Address currentAddress
    @Convert(converter = SimpleJsonConverter.class)
    Address permanentAddress

    @Convert(converter = SimpleJsonConverter.class)
    Map<String, Object> switchMetadata

    @ManyToOne
    Client client

}