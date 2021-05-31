package hyperface.cms.repository

import hyperface.cms.domains.CardBin
import hyperface.cms.domains.CreditCardScheduleOfCharges
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChargesRepository extends CrudRepository<CreditCardScheduleOfCharges, Long> {

}