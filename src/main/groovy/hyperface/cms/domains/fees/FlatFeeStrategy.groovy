package hyperface.cms.domains.fees

import javax.persistence.Entity

// https://www.bankbazaar.com/credit-card/rbl-bank-edition-credit-card.html
// See cash withdrawal charges
// 2.5% (min Rs.500) of the cash amount
@Entity
class FlatFeeStrategy extends FeeStrategy {

    Double valueTobeCharged

    @Override
    Double getFee(Double inputValue) {
        return valueTobeCharged
    }
}
