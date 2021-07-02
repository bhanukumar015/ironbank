package hyperface.cms.domains.batch

import hyperface.cms.repository.batch.CurrencyConversionRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CurrencyConversionTest {
    @Autowired
    private CurrencyConversionRepository conversionRepository;

    @Test
    @Order(1)
    void entityTest() {
        Assertions.assertTrue(conversionRepository.count() >= 0, "The currency_conversion table must exist.");
    }

    @Test
    @Order(2)
    void insertionTest() {
        CurrencyConversion currencyConversion = new CurrencyConversion();
        currencyConversion.tap {
            sourceCurrency = "USD"
            destinationCurrency = "INR"
            conversionRate = 75.58
        }
        CurrencyConversion result = conversionRepository.save(currencyConversion)
        Assertions.assertTrue(result.getSourceCurrency() == "USD")
        Assertions.assertTrue(result.getDestinationCurrency() == "INR")
        Assertions.assertTrue(result.getConversionRate() == 75.58)

        currencyConversion.tap {
            conversionRate = 78.58
        }
        result = conversionRepository.save(currencyConversion)
        Assertions.assertTrue(result.getSourceCurrency() == "USD")
        Assertions.assertTrue(result.getDestinationCurrency() == "INR")
        Assertions.assertTrue(result.getConversionRate() == 78.58)
    }

    @Test
    @Order(3)
    void retrievalWithUpdateTest() {
        CurrencyConversion currencyConversion = new CurrencyConversion();
        currencyConversion.tap {
            sourceCurrency = "EUR"
            destinationCurrency = "INR"
            conversionRate = 105.58
        }
        conversionRepository.save(currencyConversion)

        currencyConversion.tap {
            conversionRate = 95.58
        }
        CurrencyConversion savedCC = conversionRepository.save(currencyConversion)

        Optional<CurrencyConversion> result = conversionRepository.findById(savedCC.getId())
        Assertions.assertTrue(result.isPresent())
        Assertions.assertTrue(result.get().getConversionRate() == 95.58)
    }
}
