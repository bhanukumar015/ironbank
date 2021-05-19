package hyperface.cms.domains

import hyperface.cms.domains.fees.FeeStrategy

import javax.persistence.Entity
import javax.persistence.OneToOne

@Entity
class CreditCardScheduleOfCharges {

    String name

    Double joiningFee
    Double annualFee
    Double additionalCardFee
    Double emiConversionProcessingFee

    @OneToOne
    FeeStrategy lateFeeStrategy

    @OneToOne
    FeeStrategy cashAdvanceStrategy

    @OneToOne
    FeeStrategy forexFeeStrategy



}
