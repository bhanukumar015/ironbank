package hyperface.cms.repository

import hyperface.cms.domains.CardControl
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardControlRepository extends CrudRepository<CardControl, String> {
}
