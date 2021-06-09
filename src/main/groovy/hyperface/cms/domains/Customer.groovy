package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
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

    String deliveryCity
    String deliveryZipCode
    String deliveryAddress1
    String billingCity
    String billingZipCode
    String billingAddress1

    String switchMetadata

    @ManyToOne
    Client client

}
