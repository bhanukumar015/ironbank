package hyperface.cms.repository.rewards

import hyperface.cms.domains.rewards.Reward
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RewardRepository extends CrudRepository<Reward, String> {

}