package hyperface.cms.domains.fees

import com.fasterxml.jackson.annotation.JsonIgnore
import hyperface.cms.Constants

import javax.persistence.Entity

// https://www.bankbazaar.com/credit-card/rbl-bank-edition-credit-card.html
// See cash withdrawal charges
// 2.5% (min Rs.500) of the cash amount
class FlatFeeStrategy extends FeeStrategy {

    Double valueTobeCharged

    @JsonIgnore
    Constants.FeeStrategyType type = Constants.FeeStrategyType.FLAT

    @Override
    Double getFee(Double inputValue) {
        return valueTobeCharged
    }

    @Override
    Constants.FeeStrategyType getStrategyType() {
        return this.type
    }
}
