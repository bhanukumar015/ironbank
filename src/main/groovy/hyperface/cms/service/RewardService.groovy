package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.commands.Operation
import hyperface.cms.commands.RewardsRequest
import hyperface.cms.commands.RewardsResponse
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.rewards.Reward
import hyperface.cms.domains.rewards.RewardsOffer
import hyperface.cms.repository.rewards.RewardRepository
import hyperface.cms.repository.rewards.RewardsOfferRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
@Slf4j
class RewardService {

    @Autowired
    private RewardRepository rewardRepository

    @Autowired
    private RewardsOfferRepository offerRepository

    ResponseEntity<RewardsResponse> getSummary(CreditAccount creditAccount) {
        Reward reward = creditAccount.getReward()

        // return error response if no rewards program is associated with this account
        if (null == reward) {
            String errorMessage = "No rewards program associated with this credit account. ID: [" + creditAccount.getId() + "]"
            log.error("Error occurred while processing rewards summary for the accountID : [{}]. Exception: [{}]", creditAccount.getId(), errorMessage)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
        }

        RewardsOffer offer = offerRepository.findById(reward.getOffer().getId()).get()
        //return success response
        RewardsResponse rewardResponse = new RewardsResponse()
                .tap {
                    creditAccountId = creditAccount.getId()
                    currentRewardPointsBalance = reward.getRewardBalance()
                    pendingRewardPoints = reward.getRewardOnHold()
                    conversionCurrency = offer.getTargetConversionCurrency()
                    amountEquivalentToRewardPoints = offer.getCurrencyConversionRatio() * reward.getRewardBalance()
                }
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(rewardResponse)

    }

    ResponseEntity<RewardsResponse> executeOperation(final RewardsRequest request, final CreditAccount creditAccount, final Reward reward) {
        switch (request.getOperation() as Operation) {
            case Operation.DEBIT: {
                if (reward.getRewardBalance() >= request.getRewardPointsCount()
                        || request.getIgnoreInsufficientBalance()) {
                    //deduct points and update rewards in DB
                    reward.tap {
                        rewardBalance = Math.max(0, reward.getRewardBalance() - request.getRewardPointsCount())
                    }
                    Reward savedReward = rewardRepository.save(reward)
                    //return success response
                    RewardsResponse rewardResponse = new RewardsResponse()
                            .tap {
                                creditAccountId = creditAccount.getId()
                                currentRewardPointsBalance = savedReward.getRewardBalance()
                            }
                    return ResponseEntity
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(rewardResponse)

                } else {
                    // throw error, since rewards balance is insufficient for a debit operation
                    String errorMessage = "Insufficient rewards balance to debit."
                    log.error("Error occurred while processing rewards DEBIT API. Exception: [{}]", errorMessage)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
                }
            }
            case Operation.CREDIT: {
                // add points to reward balance and save in DB
                reward.tap {
                    rewardBalance = reward.getRewardBalance() + request.getRewardPointsCount()
                }
                Reward savedReward = rewardRepository.save(reward)
                //return success response
                RewardsResponse rewardResponse = new RewardsResponse()
                        .tap {
                            creditAccountId = creditAccount.getId()
                            currentRewardPointsBalance = savedReward.getRewardBalance()
                        }
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(rewardResponse)
            }
        }
    }
}
