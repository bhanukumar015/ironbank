package hyperface.cms.domains.rewards

import hyperface.cms.Constants

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity
class RewardsOffer extends Offer {

    RewardsOffer() {
        this.setOfferType(OfferType.REWARDS)
    }

    Double currencyConversionRatio
    Integer amountToSpendForRewardsInMultiple
    Integer rewardPointsToBeGained

    @Enumerated(EnumType.STRING)
    Constants.Currency targetConversionCurrency
}
