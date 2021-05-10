package hyperface.cms.domains

import javax.persistence.*

@Entity
class PrepaidAccount extends Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    Double currentBalance

    @ManyToOne
    @JoinColumn(name="customer_id")
    Customer customer

}
