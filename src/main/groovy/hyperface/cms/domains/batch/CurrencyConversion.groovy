package hyperface.cms.domains.batch

import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
class CurrencyConversion {

    @EmbeddedId
    CurrencyPair currencyPair

    Double conversionRate

    @Embeddable
    static class CurrencyPair implements Serializable {
        String sourceCurrency
        String destinationCurrency

        CurrencyPair() {
        }

        CurrencyPair(String sourceCurrency, String destinationCurrency) {
            this.sourceCurrency = sourceCurrency
            this.destinationCurrency = destinationCurrency
        }
    }
}
