package hyperface.cms.domains.fees

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import hyperface.cms.Constants

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes([
        @JsonSubTypes.Type(value = FlatFeeStrategy.class, name = "flat"),
        @JsonSubTypes.Type(value = PercentFeeStrategy.class, name = "percent"),
        @JsonSubTypes.Type(value = HigherOfPctOrMinValueStrategy.class, name = "higherOfPctOrX"),
        @JsonSubTypes.Type(value = PctWithMinAndMaxStrategy.class, name = "pctWithMinMax"),
        @JsonSubTypes.Type(value = SlabWiseStrategy.class, name = "slabWise")
])
abstract class FeeStrategy  {
    @JsonIgnore
    Constants.FeeStrategyType type

    abstract public Double getFee(Double inputValue);
    abstract public Constants.FeeStrategyType getStrategyType();
}
