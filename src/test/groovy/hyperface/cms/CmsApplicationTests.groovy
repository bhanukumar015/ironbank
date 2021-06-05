package hyperface.cms

import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.controllers.HistoryController
import hyperface.cms.domains.CreditCardScheduleOfCharges
import hyperface.cms.domains.fees.FeeSlab
import hyperface.cms.domains.fees.FeeStrategy
import hyperface.cms.domains.fees.FlatFeeStrategy
import hyperface.cms.domains.fees.HigherOfPctOrMinValueStrategy
import hyperface.cms.domains.fees.JoiningFee
import hyperface.cms.domains.fees.LatePaymentFee
import hyperface.cms.domains.fees.SlabWiseStrategy
import hyperface.cms.repository.ChargesRepository
import hyperface.cms.repository.CreditAccountRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CmsApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	ChargesRepository chargesRepository

	@Autowired
	CreditAccountRepository creditAccountRepository

	private AuthorizationRequest getBasicDebitReq() {
		AuthorizationRequest req = new AuthorizationRequest()
		req.transactionType = Constants.TxnType.AUTH
		req.billingAmount = 1000.00
		req.transactionAmount = 1000.00
		req.billingCurrency = "INR"
		req.transactionCurrency = "INR"
		req.merchantCategoryCode = "1001"
		req.merchantTerminalId = "TID001"
		req.cardId = "card_Zu3nmDeFQXMdfTYK"
		req.description = "Swiggy"
		req.merchantNameLocation = "Swiggy Bangalore"
		req.systemTraceAuditNumber = "ST0001"
		return req
	}

	@Autowired
	HistoryController historyController

	private Date addXDaysToTime(int days) {
		Calendar cal = Calendar.getInstance()
		cal.add(Calendar.DATE, days)
		return cal.getTime()
	}

	@Test
	void testHistoryController() {
		AuthorizationRequest req1 = getBasicDebitReq()
		req1.transactionDate = addXDaysToTime(-10)

		AuthorizationRequest req2 = getBasicDebitReq()
		req2.transactionDate = addXDaysToTime(-9)

		historyController.createTxn(req1)
		historyController.createTxn(req2)

		println creditAccountRepository.findById("0da1b2cb-31b0-4210-a5d0-20089db12114").get().dump()
	}

	@Test
	void testScheduleOfCharges() {
		CreditCardScheduleOfCharges charges = new CreditCardScheduleOfCharges()
		charges.name = "Test charges"

		charges.renewalFeeStrategy = new FlatFeeStrategy(valueTobeCharged: 1000)
		charges.joiningFeeStrategy = new FlatFeeStrategy(valueTobeCharged: 1000)
		charges.cashAdvanceFeeStrategy = new HigherOfPctOrMinValueStrategy(minTobeCharged: 100, percentage: 3)

		charges.joiningFee = new JoiningFee(
				applicationTrigger: JoiningFee.ApplicationTrigger.AFTER_FIRST_PURCHASE_TXN,
				feeStrategy: new FlatFeeStrategy(valueTobeCharged: 1000)
		)
		FeeStrategy lateFeeStrategy = new SlabWiseStrategy()
		lateFeeStrategy.feeSlabs = [
		        new FeeSlab(minValue: 0, maxValue: 1000, feeAmount: 50),
				new FeeSlab(minValue: 1001, maxValue: 10000, feeAmount: 200),
				new FeeSlab(minValue: 10001, maxValue: Integer.MAX_VALUE, feeAmount: 500),
		]
		charges.lateFee = new LatePaymentFee(feeStrategy: lateFeeStrategy, bufferDaysPastDue: 3)
		chargesRepository.save(charges)

		assert charges.id != null
	}

}
