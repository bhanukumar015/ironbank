package hyperface.cms

import hyperface.cms.Constants.TxnType
import hyperface.cms.Utility.MockObjects
import hyperface.cms.appdata.TxnNotEligible
import hyperface.cms.commands.CustomerTransactionRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditCardScheduleOfCharges
import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.SystemTransaction
import hyperface.cms.domains.interest.Condition
import hyperface.cms.domains.interest.InterestCriteria
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.SystemTransactionRepository
import hyperface.cms.repository.TransactionLedgerRepository
import hyperface.cms.service.FeeService
import hyperface.cms.service.PaymentService
import io.vavr.control.Either
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PaymentServiceTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	PaymentService paymentService

	@Autowired
	CardRepository cardRepository

	@Autowired
	FeeService feeService

	@Mock
	CreditAccountRepository accountRepository

	@Mock
	SystemTransactionRepository systemTransactionRepository

	@Mock
	TransactionLedgerRepository transactionLedgerRepository

	Integer annualizedPercentageRateInBps = 4500
	int feeApr = 4000

	private LedgerEntry getFeeLedgerEntry(CreditCardProgram creditCardProgram) {
		LedgerEntry ledgerEntry = new LedgerEntry()
		ledgerEntry.txnType = TxnType.FEE
		ledgerEntry.amount = 1000
		CustomerTxn customerTxn = new CustomerTxn()
		customerTxn.card = new Card()
		customerTxn.card.cardProgram = creditCardProgram
		ledgerEntry.customerTxn = customerTxn
		return ledgerEntry
	}

	private LedgerEntry getPurchaseLedgerEntry(CreditCardProgram creditCardProgram) {
		LedgerEntry ledgerEntry = new LedgerEntry()
		ledgerEntry.txnType = TxnType.PURCHASE
		ledgerEntry.amount = 2000
		CustomerTxn customerTxn = new CustomerTxn()
		customerTxn.card = new Card()
		customerTxn.card.cardProgram = creditCardProgram
		ledgerEntry.customerTxn = customerTxn
		return ledgerEntry
	}

	private CreditCardProgram getProgramWithCharges() {
		CreditCardScheduleOfCharges charges = new CreditCardScheduleOfCharges()
		CreditCardProgram creditCardProgram = new CreditCardProgram()
		creditCardProgram.annualizedPercentageRateInBps = annualizedPercentageRateInBps

		charges.name = "Test charges"

		// don't charge interest for fee transactions in the given cycle
		InterestCriteria cri1 = new InterestCriteria()
		cri1.name = "Interest on fees"
		cri1.aprInBps = feeApr
		cri1.conditions = [new Condition(parameter: Condition.Parameter.TRANSACTION_TYPE,
				matchCriteria: Condition.MatchCriteria.EQUALS,
				value: TxnType.FEE.name())]
		cri1.precedence = 10000

		InterestCriteria cri2 = new InterestCriteria()
		cri2.name = "Interest on tax"
		cri2.aprInBps = 3500
		cri2.conditions = [new Condition(parameter: Condition.Parameter.TRANSACTION_TYPE,
				matchCriteria: Condition.MatchCriteria.IN_LIST,
				value: [TxnType.TAX, TxnType.TAX_REVERSAL].collect({it.name()}).join(",") )]
		cri1.precedence = 5000

		charges.interestCriteriaList = [cri1, cri2]
		creditCardProgram.scheduleOfCharges = charges
		return creditCardProgram
	}

	@Test
	void testInterestApplication() {
		// assignment ceremony
		CreditCardProgram creditCardProgram = this.getProgramWithCharges()
		LedgerEntry feeEntry = this.getFeeLedgerEntry(creditCardProgram)
		LedgerEntry purchaseEntry = this.getPurchaseLedgerEntry(creditCardProgram)

		// logic
		assert paymentService.getInterestRateForTxn(feeEntry) == feeApr
		assert paymentService.getInterestRateForTxn(purchaseEntry) == annualizedPercentageRateInBps
	}

	@Test
	void testDomesticTransaction() {
		MockObjects mockObjects = new MockObjects()
		CustomerTransactionRequest req = mockObjects.getTestCustomerDomesticTransactionResquest()
		Card card = cardRepository.findById(req.cardId).get()
		req.card = card
		Either<TxnNotEligible,Boolean> result = paymentService.checkEligibility(req)
		Either<GenericErrorResponse, CustomerTransaction> txnResult = paymentService.createCustomerTxn(req)
		Assertions.assertTrue(result.right().get())
		Assertions.assertTrue(txnResult.right().get().billingAmount == 1500)
	}

	@Test
	void testInternationalTransaction() {
		MockObjects mockObjects = new MockObjects()
		CustomerTransactionRequest req = mockObjects.getTestCustomerInternationalTransactionResquest()
		Card card = cardRepository.findById(req.cardId).get()
		req.card = card
		Either<TxnNotEligible, Boolean> result = paymentService.checkEligibility(req)
		Either<GenericErrorResponse, CustomerTransaction> txnResult = paymentService.createCustomerTxn(req)
		Assertions.assertTrue(result.right().get())
		Assertions.assertTrue(txnResult.right().get().billingAmount >= 1170)
	}

	@Test
	void testJoiningFeeEntry(){
		feeService.creditAccountRepository = accountRepository
		feeService.systemTransactionRepository = systemTransactionRepository
		feeService.transactionLedgerRepository = transactionLedgerRepository
		Mockito.when(accountRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(systemTransactionRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(transactionLedgerRepository.save(Mockito.any())).thenReturn(null)
		MockObjects mockObjects = new MockObjects()
		CustomerTransaction txn = mockObjects.getTestCustomerTransaction()
		TransactionLedger entry = feeService.createJoiningFeeEntry(txn)
		assert entry.transactionType == LedgerTransactionType.FEE
		assert entry.transactionAmount == 100
		SystemTransaction sysTxn = entry.transaction as SystemTransaction
		assert sysTxn.feeType == FeeType.JOINING
	}

	@Test
	void testCashAdvanceFeeEntry(){
		feeService.creditAccountRepository = accountRepository
		feeService.systemTransactionRepository = systemTransactionRepository
		feeService.transactionLedgerRepository = transactionLedgerRepository
		Mockito.when(accountRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(systemTransactionRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(transactionLedgerRepository.save(Mockito.any())).thenReturn(null)
		MockObjects mockObjects = new MockObjects()
		CustomerTransaction txn = mockObjects.getTestCustomerTransaction()
		TransactionLedger entry = feeService.createCashWithdrawalFeeEntry(txn, 10000, Boolean.FALSE)
		assert entry.transactionType == LedgerTransactionType.FEE_REVERSAL
		assert entry.transactionAmount == 300
		SystemTransaction sysTxn = entry.transaction as SystemTransaction
		assert sysTxn.feeType == FeeType.CASH_ADVANCE_FEE
	}

	@Test
	void testMarkupFeeEntry(){
		feeService.creditAccountRepository = accountRepository
		feeService.systemTransactionRepository = systemTransactionRepository
		feeService.transactionLedgerRepository = transactionLedgerRepository
		Mockito.when(accountRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(systemTransactionRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(transactionLedgerRepository.save(Mockito.any())).thenReturn(null)
		MockObjects mockObjects = new MockObjects()
		CustomerTransaction txn = mockObjects.getTestCustomerTransaction()
		TransactionLedger entry = feeService.createMarkupFeeEntry(txn, 10000, Boolean.TRUE)
		assert entry.transactionType == LedgerTransactionType.FEE
		assert entry.transactionAmount == 200
		SystemTransaction sysTxn = entry.transaction as SystemTransaction
		assert sysTxn.feeType == FeeType.FOREX_MARKUP
	}

	@Test
	void testAddOnCardFeeEntry(){
		feeService.creditAccountRepository = accountRepository
		feeService.systemTransactionRepository = systemTransactionRepository
		feeService.transactionLedgerRepository = transactionLedgerRepository
		Mockito.when(accountRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(systemTransactionRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(transactionLedgerRepository.save(Mockito.any())).thenReturn(null)
		MockObjects mockObjects = new MockObjects()
		TransactionLedger entry = feeService.createAddOnCardFeeEntry(mockObjects.getTestCard())
		assert entry.transactionType == LedgerTransactionType.FEE
		assert entry.transactionAmount == 100
		SystemTransaction sysTxn = entry.transaction as SystemTransaction
		assert sysTxn.feeType == FeeType.ADD_ONCARD
	}

	@Test
	void testRewardsRedemptionFeeEntry(){
		feeService.creditAccountRepository = accountRepository
		feeService.systemTransactionRepository = systemTransactionRepository
		feeService.transactionLedgerRepository = transactionLedgerRepository
		Mockito.when(accountRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(systemTransactionRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(transactionLedgerRepository.save(Mockito.any())).thenReturn(null)
		MockObjects mockObjects = new MockObjects()
		Card card = mockObjects.getTestCard()
		card.creditAccount.cards = Arrays.asList(card)
		TransactionLedger entry = feeService.createRewardRedemptionFeeEntry(card.creditAccount)
		assert entry.transactionType == LedgerTransactionType.FEE
		assert entry.transactionAmount == 50
		SystemTransaction sysTxn = entry.transaction as SystemTransaction
		assert sysTxn.feeType == FeeType.REWARDS_REDEMPTION
	}

	@Test
	void testOverlimitFeeEntry(){
		feeService.creditAccountRepository = accountRepository
		feeService.systemTransactionRepository = systemTransactionRepository
		feeService.transactionLedgerRepository = transactionLedgerRepository
		Mockito.when(accountRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(systemTransactionRepository.save(Mockito.any())).thenReturn(null)
		Mockito.when(transactionLedgerRepository.save(Mockito.any())).thenReturn(null)
		MockObjects mockObjects = new MockObjects()
		CustomerTransaction txn = mockObjects.getTestCustomerTransaction()
		TransactionLedger entry = feeService.createOverlimitFeeEntry(txn, Boolean.TRUE)
		assert entry.transactionType == LedgerTransactionType.FEE
		assert entry.transactionAmount == 150
		SystemTransaction sysTxn = entry.transaction as SystemTransaction
		assert sysTxn.feeType == FeeType.OVERLIMIT
	}
}
