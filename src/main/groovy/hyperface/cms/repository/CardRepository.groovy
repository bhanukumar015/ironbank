package hyperface.cms.repository

import hyperface.cms.domains.Card
import hyperface.cms.domains.CardBin
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository extends CrudRepository<Card, Long> {

}