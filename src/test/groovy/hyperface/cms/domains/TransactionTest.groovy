package hyperface.cms.domains

import hyperface.cms.repository.TransactionRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TransactionTest {
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void entityCheck() {
        try {
            Assertions.assertTrue(transactionRepository.count() >= 0);
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Table: [transaction] must be present in DB.");
        }
    }
}
