package hyperface.cms.commands


import hyperface.cms.util.validation.PositiveValue
import hyperface.cms.util.validation.StringEnumeration

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class RewardsRequest {
    @StringEnumeration(enumClass = Operation.class, message = "operation must not be null/empty. Must be one of [DEBIT, CREDIT].")
    String operation
    @PositiveValue(message = "rewardPointsCount must be an integer, greater than 0")
    Integer rewardPointsCount
    @NotBlank(message = "creditAccountId must not be null/empty")
    String creditAccountId
    @NotNull(message = "ignoreInsufficientBalance must not be null/empty")
    Boolean ignoreInsufficientBalance
}

enum Operation {
    DEBIT,
    CREDIT

}
