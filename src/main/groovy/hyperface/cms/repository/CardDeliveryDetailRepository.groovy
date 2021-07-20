package hyperface.cms.repository

import hyperface.cms.domains.CardDeliveryDetail
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardDeliveryDetailRepository extends CrudRepository<CardDeliveryDetail, String> {
}
