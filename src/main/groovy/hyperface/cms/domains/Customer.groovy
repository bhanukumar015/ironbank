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

    String firstname
    String middlename
    String lastname

    String emailAddress
    String countryCode
    String mobileNumber

    @ManyToOne
    Client client

}
