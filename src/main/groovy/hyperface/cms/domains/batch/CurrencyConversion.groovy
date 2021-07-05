package hyperface.cms.domains.batch

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class CurrencyConversion {
    @Id
    @GenericGenerator(name = "currency_conversion_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "currency_conversion_id")
    String id

    String sourceCurrency
    String destinationCurrency
    Double conversionRate

}
