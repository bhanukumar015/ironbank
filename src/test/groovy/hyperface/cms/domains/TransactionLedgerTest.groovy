package hyperface.cms.domains

import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.*
import hyperface.cms.repository.TransactionLedgerRepository
import hyperface.cms.repository.TransactionRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionLedgerTest {
    @Autowired
    private TransactionLedgerRepository txnLedgerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @Order(1)
    void entityCheck() {
        try {
            Assertions.assertTrue(txnLedgerRepository.count() >= 0);
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Table: [transaction_ledger] must be present in DB.");
        }
    }

    @Test
    @Order(2)
    void ledgerSaveEntityTest() {
        Transaction txn = this.getDefaultTransactionEntity();
        TransactionLedger txnLedger = this.getDefaultTransactionLedgerEntity();
        transactionRepository.save(txn);
        txnLedger.with {
            transaction = txn;
            txnDescription = txn.getTxnDescription();
        }

        try {
            int txnLedgerRecordCount = txnLedgerRepository.count();
            TransactionLedger result = txnLedgerRepository.save(txnLedger);
            Assertions.assertTrue(txnLedgerRepository.count() == txnLedgerRecordCount + 1);
            String txnRefId = result.transaction.getId();
            Assertions.assertTrue(transactionRepository.findById(txnRefId).isPresent());
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Ledger entry must have been persisted in transaction_ledger table.");
        }

    }

    @Test
    @Order(3)
    void manyToOneAssociationTest() {
        try {
            int txnLedgerRecordCount = txnLedgerRepository.count();
            TransactionLedger transactionLedger = this.getDefaultTransactionLedgerEntity();
            Transaction oneTxnEntity = transactionRepository.findAll().asList().stream().findFirst().get();
            TransactionLedger oneTxnLedgerEntity = txnLedgerRepository.findAll().asList().stream().findFirst().get();
            final List<String> ledgerEntryIds = new ArrayList<>();
            ledgerEntryIds.add(oneTxnLedgerEntity.getId());
            oneTxnLedgerEntity.with {
                id = null;
                transaction = oneTxnEntity;
                // change few relevant entries to make a valid new txn_ledger entry
                closingBalance = 0.0
            }
            TransactionLedger result = txnLedgerRepository.save(oneTxnLedgerEntity);
            Assertions.assertTrue(txnLedgerRepository.count() == txnLedgerRecordCount + 1);
            ledgerEntryIds.add(result.getId());

            // verify manyToOne association
            String txnRefId = oneTxnEntity.getId();
            List<TransactionLedger> listTxnLedger = txnLedgerRepository.findAllByTxnRefId(txnRefId);
            Assertions.assertTrue(listTxnLedger.size() >= 2);
            Assertions.assertTrue(listTxnLedger
                    .stream()
                    .filter(tl -> ledgerEntryIds
                            .contains(tl.getId())).count() >= 2);
        } catch (Exception e) {
            Assertions.fail("Exception should not occur. Ledger entry must have been persisted in transaction_ledger table.");
        }
    }

    private Transaction getDefaultTransactionEntity() {
        return new Transaction()
                .tap {
                    accountNumber = "123456789"
                    cardHash = "66rggtr552883jju363yhh3y"
                    txnDate = LocalDateTime.now()
                    txnPostingDate = LocalDate.now().plusDays(1)
                    txnAmount = 200.50
                    pendingTxnAmount = 200.50
                    authCode = "4567"
                    tid = "terminalID1"
                    mid = "merchantId"
                    mcc = "6732"
                    referenceNumber = UUID.randomUUID()
                    txnDescription = "Sample Test Transaction"
                    schemeReferenceId = UUID.randomUUID()
                    merchantCountryCode = "01"
                    currencyCode = "USD"
                    txnAmountSrc = 2.70
                    posEntryMode = "05"
                    cashbackFundingAccountRef = UUID.randomUUID()
                    hasExecuted = Boolean.TRUE
                    executeAfter = LocalDateTime.now()
                    executedOn = LocalDateTime.now()
                    transactionType = TransactionType.AUTHORIZE
                    authorizationType = AuthorizationType.PURCHASE
                    onUsOffUsIndicator = OnUsOffUsIndicator.ONUS
                    sovereigntyIndicator = SovereigntyIndicator.INTERNATIONAL
                    feeType = FeeType.ANNUAL
                    txnStatus = TransactionStatus.APPROVED
                    txnSourceIndicator = TransactionSourceIndicator.C
                }
    }

    private TransactionLedger getDefaultTransactionLedgerEntity() {
        return new TransactionLedger()
                .tap {
                    accountNumber = "213421223456"
                    postingDate = LocalDate.now()
                    openingBalance = 200.5
                    closingBalance = 200.5
                    moneyMovementIndicator = MoneyMovementIndicator.DEBIT
                    transactionType = LedgerTransactionType.PURCHASE
                    onUsOffUsIndicator = OnUsOffUsIndicator.ONUS
                    feeType = FeeType.ANNUAL
                    transactionAmount = 200.50
                }
    }
}
