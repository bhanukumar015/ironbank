package hyperface.cms.commands


import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class RewardsResponse {
    String creditAccountId
    Integer currentRewardPointsBalance
    Integer pendingRewardPoints
    String conversionCurrency
    Double amountEquivalentToRewardPoints
}
