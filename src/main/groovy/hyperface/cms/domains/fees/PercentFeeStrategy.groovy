package hyperface.cms.domains.fees

import javax.persistence.Entity

// https://www.bankbazaar.com/credit-card/rbl-bank-edition-credit-card.html
// See cash withdrawal charges
// 2.5% (min Rs.500) of the cash amount
@Entity
class PercentFeeStrategy extends FeeStrategy {

    Double percentageToBeCharged

    @Override
    Double getFee(Double inputValue) {
        return inputValue * percentageToBeCharged/100
    }
}
