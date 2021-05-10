package hyperface.cms.domains

import hyperface.cms.Constants

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class CardProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    String name
    Integer annualizedPercentageRate
    Integer joiningFees
    Constants.Currency baseCurrency
    Integer renewalFees

    // program defaults
    Integer defaultDailyTransactionLimit
    Integer defaultDailyCashWithdrawalLimit

    @ManyToOne
    CardBin cardBin

}
