package hyperface.cms

import hyperface.cms.Constants.TxnType
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditCardScheduleOfCharges
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.interest.Condition
import hyperface.cms.domains.interest.InterestCriteria
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.service.PaymentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PaymentServiceTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	PaymentService paymentService

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
		creditCardProgram.scheduleOfCharges = charges
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
}
