package hyperface.cms.domains.fees

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class HigherOfPctOrMaxValueStrategy extends FeeStrategy {

    Double percentage
    Double maxAllowed

    @Override
    Double getFee(Double inputValue) {
        return Math.max(percentage/100 * inputValue, maxAllowed)
    }
}
