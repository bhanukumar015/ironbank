package hyperface.cms.domains.fees

import hyperface.cms.domains.CardStatement

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class PctWithMinAndMaxStrategy extends FeeStrategy {

    Integer percentOfOutstanding
    Double minFee
    Double maxFee

    @Override
    Double getFee(Double inputValue) {
        Double percentValue = (inputValue * percentOfOutstanding/100)
        return percentValue > minFee ? Math.min(percentOfOutstanding, maxFee) : minFee
    }
}
