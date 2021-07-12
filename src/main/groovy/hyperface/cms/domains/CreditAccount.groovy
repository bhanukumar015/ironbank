package hyperface.cms.domains


import hyperface.cms.domains.rewards.Reward
import hyperface.cms.model.enums.RepaymentIndicator
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import java.time.ZonedDateTime

@Entity
class CreditAccount extends Account {
    @Id
    @GenericGenerator(name = "credit_account_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "credit_account_id")
    String id

    String defaultCurrency
    Double approvedCreditLimit
    Double availableCreditLimit
    Boolean allowTxnLog = true
    Date lastTxnDate
    ZonedDateTime currentBillingStartDate
    ZonedDateTime currentBillingEndDate
    Integer currentBillingCycle
    String lastStatementId
    ZonedDateTime lastStatementDueDate
    ZonedDateTime lastStatementGeneratedOn


    @Enumerated(EnumType.STRING)
    RepaymentIndicator currentCycleRepaymentIndicator

    @Enumerated(EnumType.STRING)
    RepaymentIndicator previousCycleRepaymentIndicator

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customer customer

    @OneToMany(mappedBy = "creditAccount")
    List<Card> cards

    @OneToOne
    @JoinColumn(name = "reward_id", referencedColumnName = "id")
    Reward reward

}
