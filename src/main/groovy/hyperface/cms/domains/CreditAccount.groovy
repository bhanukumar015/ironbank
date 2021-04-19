package hyperface.cms.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class CreditAccount extends Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    Double approvedCreditLimit
    Double availableCreditLimit

    @ManyToOne
    @JoinColumn(name="customer_id")
    Customer customer

}
