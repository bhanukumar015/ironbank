package hyperface.cms.domains.fees

import hyperface.cms.Constants

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

// https://www.bankbazaar.com/credit-card/rbl-bank-edition-credit-card.html
// See cash withdrawal charges
// 2.5% (min Rs.500) of the cash amount
class HigherOfPctOrMinValueStrategy extends FeeStrategy {

    Double percentage
    Double minTobeCharged

    Constants.FeeStrategyType type = Constants.FeeStrategyType.HIGHER_OF_PCT_OR_X

    @Override
    Double getFee(Double inputValue) {
        return Math.max(percentage/100 * inputValue, minTobeCharged)
    }

    @Override
    Constants.FeeStrategyType getStrategyType() {
        return this.type
    }
}
