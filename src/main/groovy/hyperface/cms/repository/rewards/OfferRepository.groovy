package hyperface.cms.repository.rewards

import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository

@NoRepositoryBean
interface OfferRepository<T> extends Repository<T, String> {
}
