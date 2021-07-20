package hyperface.cms.repository.batch


import hyperface.cms.domains.batch.CurrencyConversion
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyConversionRepository extends CrudRepository<CurrencyConversion, String> {

    @Query("select cc from CurrencyConversion cc where cc.sourceCurrency = ?1 and cc.destinationCurrency = ?2")
    CurrencyConversion findBySourceCurrencyAndDestinationCurrency(String sourceCurrency, String destinationCurrency)
}