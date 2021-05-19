package hyperface.cms.domains

import hyperface.cms.Constants
import hyperface.cms.domains.fees.FeeStrategy
import hyperface.cms.domains.fees.PctWithMinAndMaxStrategy
import hyperface.cms.domains.fees.PercentFeeStrategy
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
class CardProgram extends HyperfaceProgram {
    @Id
    @GenericGenerator(name = "card_program_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "card_program_id")
    String id

    String name

    @Enumerated(value = EnumType.STRING)
    Constants.Currency baseCurrency

    Integer annualizedPercentageRate
    Double joiningFees
    Double annualFees // applicable from second year onwards

    // program defaults
    Integer defaultDailyTransactionLimit
    Integer defaultDailyCashWithdrawalLimit

    @OneToOne
    FeeStrategy lateFeeStrategy

    @OneToOne
    FeeStrategy cashAdvanceStrategy

    @OneToOne
    FeeStrategy forexFeeStrategy

    @OneToOne
    FeeStrategy joiningFeeStrategy


//    @ManyToOne
//    CreditCardScheduleOfCharges scheduleOfCharges

    @ManyToOne
    CardBin cardBin

    static CardProgram exampleCardProgram() {
        CardProgram cp = new CardProgram()
        cp.lateFeeStrategy = new PctWithMinAndMaxStrategy(minFee: 50.00, maxFee: 1250, percentOfOutstanding: 15)
        cp.forexFeeStrategy = new PercentFeeStrategy(percentageToBeCharged: 1.2)
        return cp
    }

}
