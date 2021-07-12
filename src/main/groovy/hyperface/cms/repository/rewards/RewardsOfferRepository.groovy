package hyperface.cms.repository.rewards

import hyperface.cms.domains.rewards.RewardsOffer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RewardsOfferRepository extends OfferRepository<RewardsOffer>, CrudRepository<RewardsOffer, String> {

}