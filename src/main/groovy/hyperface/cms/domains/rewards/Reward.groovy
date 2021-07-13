package hyperface.cms.domains.rewards

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Reward {
    @Id
    @GenericGenerator(name = "reward_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "reward_id")
    String id

    Integer rewardBalance
    Integer rewardOnHold

    @ManyToOne(optional = false)
    @JoinColumn(name = "offer_id", referencedColumnName = "id")
    Offer offer
}
