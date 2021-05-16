package hyperface.cms.domains.fees

import hyperface.cms.domains.CardStatement

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class SlabWiseStrategy extends FeeStrategy {

    @OneToMany
    Set<FeeSlab> feeSlabs

    @Override
    public Double getFee(Double inputValue) {
        return feeSlabs.find {
            return it.minValue < inputValue && inputValue <= it.maxValue
        }?.feeAmount ?: 0
    }

}