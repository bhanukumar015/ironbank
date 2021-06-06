package hyperface.cms.domains

import hyperface.cms.domains.converters.FeeStrategyJsonConverter
import hyperface.cms.domains.converters.SimpleJsonConverter
import hyperface.cms.domains.fees.FeeStrategy
import hyperface.cms.domains.fees.JoiningFee
import hyperface.cms.domains.fees.LatePaymentFee
import hyperface.cms.domains.interest.InterestCriteria

import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class CreditCardScheduleOfCharges {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    String name

    enum FeeItem { JOINING_FEE,
                    RENEWAL_FEE,
                    ADDON_CARD_FEE,
                    EMI_PROCESSING_FEE,
                    LATE_PAYMENT_FEE,
                    CASH_ADVANCE_FEE,
                    FOREX_FEE
    }

    @Convert(converter = FeeStrategyJsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy joiningFeeStrategy

    @Convert(converter = FeeStrategyJsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy renewalFeeStrategy

    @Convert(converter = FeeStrategyJsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy addonCardFeeStrategy

    @Convert(converter = FeeStrategyJsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy emiProcessingFeeStrategy

    @Convert(converter = SimpleJsonConverter.class)
    @Column(columnDefinition="JSON")
    LatePaymentFee lateFee

    @Convert(converter = SimpleJsonConverter.class)
    @Column(columnDefinition = "JSON")
    JoiningFee joiningFee

    @Convert(converter = FeeStrategyJsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy cashAdvanceFeeStrategy

    @Convert(converter = FeeStrategyJsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy forexFeeStrategy

//    List<FeeStrategy> dynamicFeeStructures



    @Convert(converter = SimpleJsonConverter.class)
    @Column(columnDefinition = "JSON")
    List<InterestCriteria> interestCriteriaList

}
