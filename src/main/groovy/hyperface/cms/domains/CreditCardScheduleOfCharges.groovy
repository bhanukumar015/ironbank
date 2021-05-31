package hyperface.cms.domains

import hyperface.cms.domains.fees.FeeStrategy

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

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy joiningFeeStrategy

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy renewalFeeStrategy

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy addonCardFeeStrategy

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy emiProcessingFeeStrategy

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy lateFeeStrategy

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy cashAdvanceStrategy

    @Convert(converter = JsonConverter.class)
    @Column(columnDefinition="TEXT")
    FeeStrategy forexFeeStrategy

}
