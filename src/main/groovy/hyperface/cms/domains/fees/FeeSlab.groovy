package hyperface.cms.domains.fees

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
class FeeSlab {
    Double minValue
    Double maxValue
    Double feeAmount
}
