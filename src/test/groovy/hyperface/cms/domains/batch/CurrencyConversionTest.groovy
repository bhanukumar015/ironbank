package hyperface.cms.domains.batch

import hyperface.cms.repository.batch.CurrencyConversionRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CurrencyConversionTest {
    @Autowired
    private CurrencyConversionRepository conversionRepository;

    @Test
    void entityTest() {
        Assertions.assertTrue(conversionRepository.count() >= 0, "The currency_conversion table must exist.");
    }

    @Test
    void insertionTest() {
        CurrencyConversion currencyConversion = new CurrencyConversion();
        currencyConversion.tap {
            currencyPair = new CurrencyPair("USD", "INR")
            conversionRate = 75.58
        }
        CurrencyConversion result = conversionRepository.save(currencyConversion)
        Assertions.assertTrue(result.currencyPair.getSourceCurrency() == "USD")
        Assertions.assertTrue(result.currencyPair.getDestinationCurrency() == "INR")
        Assertions.assertTrue(result.getConversionRate() == 75.58)

        currencyConversion.tap {
            conversionRate = 78.58
        }
        result = conversionRepository.save(currencyConversion)
        Assertions.assertTrue(result.currencyPair.getSourceCurrency() == "USD")
        Assertions.assertTrue(result.currencyPair.getDestinationCurrency() == "INR")
        Assertions.assertTrue(result.getConversionRate() == 78.58)
    }

    @Test
    void retrievalWithUpdateTest() {
        CurrencyConversion currencyConversion = new CurrencyConversion();
        currencyConversion.tap {
            currencyPair = new CurrencyPair("EUR", "INR")
            conversionRate = 105.58
        }
        conversionRepository.save(currencyConversion)

        currencyConversion.tap {
            conversionRate = 95.58
        }
        conversionRepository.save(currencyConversion)

        Optional<CurrencyConversion> result = conversionRepository.findById(new CurrencyPair("EUR", "INR"))
        Assertions.assertTrue(result.isPresent())
        Assertions.assertTrue(result.get().getConversionRate() == 95.58)
    }
}
