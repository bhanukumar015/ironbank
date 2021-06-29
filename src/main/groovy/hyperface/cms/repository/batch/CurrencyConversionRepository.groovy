package hyperface.cms.repository.batch


import hyperface.cms.domains.batch.CurrencyConversion
import hyperface.cms.domains.batch.CurrencyPair
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyConversionRepository extends CrudRepository<CurrencyConversion, CurrencyPair> {

}