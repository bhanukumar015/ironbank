package hyperface.cms.domains

import hyperface.cms.Utility.MockObjects
import hyperface.cms.appdata.TxnNotEligible
import hyperface.cms.commands.CustomerTransactionRequest
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerTransactionRepository
import hyperface.cms.repository.SystemTransactionRepository
import hyperface.cms.service.PaymentService
import io.vavr.control.Either
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TransactionTest {
    @Autowired
    private CustomerTransactionRepository transactionRepository

    @Autowired
    private SystemTransactionRepository systemTxnRepository

    @Autowired
    private CreditAccountRepository creditAccountRepository

    @Autowired
    private CardRepository cardRepository

    @Autowired
    private PaymentService paymentService

    @Test
    void entityCheck() {
        try {
            Assertions.assertTrue(transactionRepository.count() >= 0)
            Assertions.assertTrue(systemTxnRepository.count() >= 0)
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Table: [transaction] must be present in DB.")
        }
    }

    @Test
    void testDomesticTransaction() {
        MockObjects mockObjects = new MockObjects()
        CustomerTransactionRequest req = mockObjects.getTestCustomerDomesticTransactionResquest()
        Card card = cardRepository.findById(req.cardId).get()
        req.card = card
        Either<TxnNotEligible,Boolean> result = paymentService.checkEligibility(req)
        Either<String,CustomerTransaction> txnResult = paymentService.createCustomerTxn(req)
        Assertions.assertTrue(result.right().get())
        Assertions.assertTrue(txnResult.right().get().billingAmount == 1500)
    }

    @Test
    void testInternationalTransaction() {
        MockObjects mockObjects = new MockObjects()
        CustomerTransactionRequest req = mockObjects.getTestCustomerInternationalTransactionResquest()
        Card card = cardRepository.findById(req.cardId).get()
        req.card = card
        Either<TxnNotEligible,Boolean> result = paymentService.checkEligibility(req)
        Either<String,CustomerTransaction> txnResult = paymentService.createCustomerTxn(req)
        Assertions.assertTrue(result.right().get())
        Assertions.assertTrue(txnResult.right().get().billingAmount >= 1170)
    }
}
