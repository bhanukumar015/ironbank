package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator

import javax.persistence.*

@Entity
class PrepaidAccount extends Account {
    @Id
    @GenericGenerator(name = "prepaid_account_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "prepaid_account_id")
    String id

    Double currentBalance

    @ManyToOne
    @JoinColumn(name="customer_id")
    Customer customer

}
