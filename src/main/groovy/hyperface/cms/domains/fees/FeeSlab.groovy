package hyperface.cms.domains.fees

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class FeeSlab {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    Double minValue
    Double maxValue
    Double feeAmount
}
