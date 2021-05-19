package hyperface.cms.domains

import hyperface.cms.domains.fees.FeeStrategy

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
class CreditCardScheduleOfCharges {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

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

    @ManyToOne
    Bank bank

}
