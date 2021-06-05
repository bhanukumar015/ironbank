package hyperface.cms.domains

import hyperface.cms.Constants
import org.hibernate.annotations.GenericGenerator

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
    @GenericGenerator(name = "credit_account_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "credit_account_id")
    String id

    Constants.Currency defaultCurrency
    Double approvedCreditLimit
    Double availableCreditLimit

    Boolean allowTxnLog = true
    Date lastTxnDate

    @ManyToOne
    @JoinColumn(name="customer_id")
    Customer customer

}
