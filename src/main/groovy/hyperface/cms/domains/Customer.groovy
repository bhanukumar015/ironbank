package hyperface.cms.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    String firstname
    String middlename
    String lastname

    String emailAddress
    String countryCode
    String mobileNumber

}
