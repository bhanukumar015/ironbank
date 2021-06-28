package hyperface.cms.domains

import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.AuthorizationType
import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.OnUsOffUsIndicator
import hyperface.cms.model.enums.SovereigntyIndicator
import hyperface.cms.model.enums.TransactionSourceIndicator
import hyperface.cms.model.enums.TransactionStatus
import hyperface.cms.model.enums.TransactionType
import hyperface.cms.repository.CustomerTransactionRepository
import hyperface.cms.repository.TransactionLedgerRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionLedgerTest {
    @Autowired
    private TransactionLedgerRepository txnLedgerRepository

    @Autowired
    private CustomerTransactionRepository transactionRepository

    /**
     * Uncomment and use below to clear tables in DB
     */
/*
    @Test
    void clear() {
        txnLedgerRepository.deleteAll()
        transactionRepository.deleteAll()
    }
*/

    @Test
    @Order(1)
    void entityCheck() {
        try {
            Assertions.assertTrue(txnLedgerRepository.count() >= 0)
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Table: [transaction_ledger] must be present in DB.")
        }
    }

    @Test
    @Order(2)
    void ledgerSaveEntityTest() {
        CustomerTransaction txn = this.getDefaultCustomerTransactionEntity()
        TransactionLedger txnLedger = this.getDefaultTransactionLedgerEntity()
        transactionRepository.save(txn)
        txnLedger.with {
            transaction = txn
            txnDescription = txn.getTxnDescription()
        }

        try {
            int txnLedgerRecordCount = txnLedgerRepository.count()
            TransactionLedger result = txnLedgerRepository.save(txnLedger)
            Assertions.assertTrue(txnLedgerRepository.count() == txnLedgerRecordCount + 1)
            String txnRefId = result.transaction.getId()
            Assertions.assertTrue(transactionRepository.findById(txnRefId).isPresent())
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Ledger entry must have been persisted in transaction_ledger table.")
        }

    }

    @Test
    @Order(3)
    void manyToOneAssociationTest() {
        try {
            int txnLedgerRecordCount = txnLedgerRepository.count()
            CustomerTransaction oneTxnEntity = transactionRepository.findAll().asList().stream().findFirst().get()
            TransactionLedger oneTxnLedgerEntity = txnLedgerRepository.findAll().asList().stream().findFirst().get()
            final List<String> ledgerEntryIds = new ArrayList<>()
            ledgerEntryIds.add(oneTxnLedgerEntity.getId())
            oneTxnLedgerEntity.with {
                id = null
                transaction = oneTxnEntity
                // change few relevant entries to make a valid new txn_ledger entry
                closingBalance = 0.0
            }
            TransactionLedger result = txnLedgerRepository.save(oneTxnLedgerEntity)
            Assertions.assertTrue(txnLedgerRepository.count() == txnLedgerRecordCount + 1)
            ledgerEntryIds.add(result.getId())

            // verify manyToOne association
            String txnRefId = oneTxnEntity.getId()
            List<TransactionLedger> listTxnLedger = txnLedgerRepository.findAllByTxnRefId(txnRefId)
            Assertions.assertTrue(listTxnLedger.size() >= 2)
            Assertions.assertTrue(listTxnLedger
                    .stream()
                    .filter(tl -> ledgerEntryIds
                            .contains(tl.getId())).count() >= 2)
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Ledger entry must have been persisted in transaction_ledger table.")
        }
    }

    private CustomerTransaction getDefaultCustomerTransactionEntity() {
        return new CustomerTransaction()
                .tap {
                    accountNumber = "123456789"
                    cardId = "66rggtr552883jju363yhh3y"
                    txnDate = ZonedDateTime.now(ZoneId.of("UTC+0530"))
                    txnPostingDate = ZonedDateTime.now(ZoneId.of("UTC+0530")).plusDays(1)
                    transactionAmount = 200.50
                    transactionCurrency = "INR"
                    pendingTxnAmount = 200.50
                    authCode = "4567"
                    tid = "terminalID1"
                    mid = "merchantId"
                    mcc = "6732"
                    rootReferenceId = UUID.randomUUID()
                    txnDescription = "Sample Test Transaction"
                    schemeReferenceId = UUID.randomUUID()
                    merchantCountryCode = "01"
                    billingCurrency = "USD"
                    billingAmount = 2.70
                    posEntryMode = "05"
                    transactionType = TransactionType.AUTHORIZE
                    authorizationType = AuthorizationType.PURCHASE
                    onusOffusIndicator = OnUsOffUsIndicator.ONUS
                    sovereigntyIndicator = SovereigntyIndicator.INTERNATIONAL
                    txnStatus = TransactionStatus.APPROVED
                    txnSourceIndicator = TransactionSourceIndicator.CUSTOMER_INITIATED
                }
    }

    private TransactionLedger getDefaultTransactionLedgerEntity() {
        return new TransactionLedger()
                .tap {
                    accountNumber = "213421223456"
                    postingDate = ZonedDateTime.now(ZoneId.of("UTC+0530"))
                    openingBalance = 200.5
                    closingBalance = 200.5
                    moneyMovementIndicator = MoneyMovementIndicator.DEBIT
                    transactionType = LedgerTransactionType.PURCHASE
                    onusOffusIndicator = OnUsOffUsIndicator.ONUS
                    feeType = FeeType.ANNUAL
                    transactionAmount = 200.50
                }
    }
}
