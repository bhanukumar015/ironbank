package hyperface.cms.domains

import hyperface.cms.Constants
import hyperface.cms.domains.fees.FeeStrategy

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
class CardProgram extends HyperfaceProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    String name
    Constants.Currency baseCurrency

    Integer annualizedPercentageRate
    Integer joiningFees
    Integer annualFees // applicable from second year onwards

    // program defaults
    Integer defaultDailyTransactionLimit
    Integer defaultDailyCashWithdrawalLimit

    @OneToOne
    FeeStrategy lateFeeStrategy

    @OneToOne
    FeeStrategy cashAdvanceStrategy

    @ManyToOne
    CardBin cardBin

}
