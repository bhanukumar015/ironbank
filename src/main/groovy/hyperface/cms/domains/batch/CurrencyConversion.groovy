package hyperface.cms.domains.batch

import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
class CurrencyConversion {

    @EmbeddedId
    CurrencyPair currencyPair

    Double conversionRate
}
