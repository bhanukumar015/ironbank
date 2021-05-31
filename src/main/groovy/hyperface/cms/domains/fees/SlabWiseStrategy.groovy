package hyperface.cms.domains.fees

import com.fasterxml.jackson.annotation.JsonIgnore
import hyperface.cms.Constants
import hyperface.cms.domains.CardStatement

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

// https://www.hdfcbank.com/content/api/contentstream-id/723fb80a-2dde-42a3-9793-7ae1be57c87f/1b30c4ae-06ad-4270-bf05-f25774189a3a?
// HDFC Infinia
// See late payment charges
class SlabWiseStrategy extends FeeStrategy {


//    @JsonIgnore
    List<FeeSlab> feeSlabs

    Constants.FeeStrategyType type = Constants.FeeStrategyType.SLAB_FLAT

    @Override
    Double getFee(Double inputValue) {
        return feeSlabs.find {
            return it.minValue < inputValue && inputValue <= it.maxValue
        }?.feeAmount ?: 0
    }

    @Override
    Constants.FeeStrategyType getStrategyType() {
        return this.type
    }

}