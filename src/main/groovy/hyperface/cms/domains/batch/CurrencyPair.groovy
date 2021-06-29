package hyperface.cms.domains.batch


import javax.persistence.Embeddable

@Embeddable
class CurrencyPair implements Serializable {
    String sourceCurrency
    String destinationCurrency

    CurrencyPair() {
    }

    CurrencyPair(String sourceCurrency, String destinationCurrency) {
        this.sourceCurrency = sourceCurrency
        this.destinationCurrency = destinationCurrency
    }
}
