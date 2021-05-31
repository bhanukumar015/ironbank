package hyperface.cms

import hyperface.cms.domains.CreditCardScheduleOfCharges
import hyperface.cms.domains.fees.FeeSlab
import hyperface.cms.domains.fees.FeeStrategy
import hyperface.cms.domains.fees.FlatFeeStrategy
import hyperface.cms.domains.fees.HigherOfPctOrMinValueStrategy
import hyperface.cms.domains.fees.SlabWiseStrategy
import hyperface.cms.repository.ChargesRepository
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

	@Test
	void testScheduleOfCharges() {
		CreditCardScheduleOfCharges charges = new CreditCardScheduleOfCharges()
		charges.name = "Test charges"


		FeeStrategy joiningFeeStrategy = new FlatFeeStrategy()
		joiningFeeStrategy.valueTobeCharged = 1000
		charges.joiningFeeStrategy = joiningFeeStrategy

		FeeStrategy renewalFeeStrategy = new HigherOfPctOrMinValueStrategy()
		renewalFeeStrategy.minTobeCharged = 100
		renewalFeeStrategy.percentage = 10
		charges.renewalFeeStrategy = renewalFeeStrategy
		chargesRepository.save(charges)

		FeeStrategy lateFeeStrategy = new SlabWiseStrategy()
		lateFeeStrategy.feeSlabs = [
		        new FeeSlab(minValue: 0, maxValue: 1000, feeAmount: 50),
				new FeeSlab(minValue: 1001, maxValue: 10000, feeAmount: 200),
				new FeeSlab(minValue: 10001, maxValue: Integer.MAX_VALUE, feeAmount: 500),
		]
		charges.lateFeeStrategy = lateFeeStrategy
		chargesRepository.save(charges)

		assert charges.id != null
	}

}
