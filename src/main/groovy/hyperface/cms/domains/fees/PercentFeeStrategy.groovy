package hyperface.cms.domains.fees

import hyperface.cms.Constants

import javax.persistence.Entity

// https://www.bankbazaar.com/credit-card/rbl-bank-edition-credit-card.html
// See cash withdrawal charges
// 2.5% (min Rs.500) of the cash amount
class PercentFeeStrategy extends FeeStrategy {

    Double percentageToBeCharged

    Constants.FeeStrategyType type = Constants.FeeStrategyType.PERCENTAGE

    @Override
    Double getFee(Double inputValue) {
        return inputValue * percentageToBeCharged/100
    }

    @Override
    Constants.FeeStrategyType getStrategyType() {
        return this.type
    }
}
