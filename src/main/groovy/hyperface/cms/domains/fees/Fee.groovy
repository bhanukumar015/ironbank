package hyperface.cms.domains.fees

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes([
        @JsonSubTypes.Type(value = LatePaymentFee.class, name = "latePaymentFee"),
])
abstract class Fee {
}
