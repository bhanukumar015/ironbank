package hyperface.cms.repository.batch


import hyperface.cms.domains.batch.CurrencyConversion

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyConversionRepository extends CrudRepository<CurrencyConversion, String> {

}