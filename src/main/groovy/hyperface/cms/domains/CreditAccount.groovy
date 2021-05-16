package hyperface.cms.domains

import hyperface.cms.Constants

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class CreditAccount extends Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    Constants.Currency defaultCurrency
    Double approvedCreditLimit
    Double availableCreditLimit

    @ManyToOne
    @JoinColumn(name="customer_id")
    Customer customer

    @ManyToOne
    CardProgram cardProgram

}
