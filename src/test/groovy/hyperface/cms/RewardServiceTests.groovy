package hyperface.cms

import hyperface.cms.Utility.MockObjects
import hyperface.cms.commands.rewards.Operation
import hyperface.cms.commands.rewards.RewardsRequest
import hyperface.cms.commands.rewards.RewardsResponse
import hyperface.cms.domains.Account
import hyperface.cms.domains.Card
import hyperface.cms.domains.Client
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditCardScheduleOfCharges
import hyperface.cms.domains.Customer
import hyperface.cms.domains.Transaction
import hyperface.cms.domains.rewards.Offer
import hyperface.cms.domains.rewards.Reward
import hyperface.cms.domains.rewards.RewardsOffer
import hyperface.cms.model.enums.TransactionType
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.ChargesRepository
import hyperface.cms.repository.ClientRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.repository.rewards.OfferRepository
import hyperface.cms.repository.rewards.RewardRepository
import hyperface.cms.repository.rewards.RewardsOfferRepository
import hyperface.cms.service.RewardService
import io.vavr.control.Try
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
public class RewardServiceTests {
    @Autowired
    ChargesRepository chargesRepository;

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    CardProgramRepository cardProgramRepository;

    @Autowired
    OfferRepository offerRepository

    @Autowired
    RewardsOfferRepository rewardsOfferRepository

    @Autowired
    RewardRepository rewardRepository

    @Autowired
    RewardService rewardService

    @Test
    void sampleTest(){
        println "hi"
        assert true == (1 == 1)
        assert false == (1 == 2)
    }

    @Test
    void getSummaryTests(){
        MockObjects mockObjects = new MockObjects()

        CreditCardProgram creditCardPro = mockObjects.getTestCreditCardProgram()
        creditCardPro.setScheduleOfCharges(null)

        println creditCardPro.dump()
        CreditCardProgram savedCreditCardPro = cardProgramRepository.save(creditCardPro)

        CreditAccount creditAccount = mockObjects.getTestCreditAccount()

        RewardsOffer offerObj = new RewardsOffer().tap {
            offerType = Offer.OfferType.REWARDS
            currencyConversionRatio = new Double(1)
            amountToSpendForRewardsInMultiple = new Integer(200)
            rewardPointsToBeGained = new Integer(50)
            targetConversionCurrency = Constants.Currency.INR
            creditCardProgram = savedCreditCardPro
        }

        rewardsOfferRepository.save(offerObj)

        Reward rewardObj = new Reward().tap {
            rewardBalance = 400
            rewardOnHold = 60
            offer = offerObj
        }

        rewardRepository.save(rewardObj);
        creditAccount = creditAccount.tap{
            reward = rewardObj
        }

        RewardsResponse rewardsResponse = rewardService.getSummary(creditAccount)

        println rewardsResponse.dump()

        assert rewardsResponse.creditAccountId == '1'
        assert  rewardsResponse.currentRewardPointsBalance == 400
        assert rewardsResponse.pendingRewardPoints == 60
        assert  rewardsResponse.getConversionCurrency() == 'INR'
        assert rewardsResponse.amountEquivalentToRewardPoints == 400.0
    }

    @Test
    void executeOperationTests(){
        MockObjects mockObjects = new MockObjects()
        CreditAccount creditAccount = mockObjects.getTestCreditAccount()
        creditAccount.setCustomer(null)
        CreditCardProgram creditCardPro = mockObjects.getTestCreditCardProgram()
        CreditCardScheduleOfCharges creditCardScheduleOfCharges = creditCardPro.scheduleOfCharges

        chargesRepository.save(creditCardScheduleOfCharges)

        println "Charges inserted"

        CreditCardProgram savedCreditCardProgram = cardProgramRepository.save(creditCardPro)

        println "credit card program inserted"

        Card card = new Card().tap {
            cardProgram = savedCreditCardProgram
            isPrimaryCard = true
        }
        creditAccount = creditAccount.tap {
            cards = Arrays.asList(card)
        }

        RewardsOffer offerObj = new RewardsOffer().tap {
            offerType = Offer.OfferType.FESTIVE_OFFER
            currencyConversionRatio = new Double(0.3)
            amountToSpendForRewardsInMultiple = new Integer(200)
            rewardPointsToBeGained = new Integer(50)
            targetConversionCurrency = Constants.Currency.INR
            creditCardProgram = savedCreditCardProgram
        }

        RewardsOffer off = rewardsOfferRepository.save(offerObj)

        Reward reward = new Reward().tap {
            rewardBalance = 400
            rewardOnHold = 60
            offer = off
        }

        Reward reward2 = new Reward().tap {
            rewardBalance = 400
            rewardOnHold = 60
            offer = off
        }

        RewardsRequest req1 = new RewardsRequest().tap{
            operation = Operation.DEBIT
            creditAccountId = creditAccount.id
            ignoreInsufficientBalance = false
            rewardPointsCount = 10
        }

        RewardsRequest req2 = new RewardsRequest().tap{
            operation = Operation.CREDIT
            creditAccountId = creditAccount.id
            ignoreInsufficientBalance = false
            rewardPointsCount = 10
        }

        RewardsResponse res1 = rewardService.executeOperation(req1, creditAccount, reward)

        RewardsResponse res2 = rewardService.executeOperation(req2, creditAccount, reward2)

        assert res1.creditAccountId == '1'
        assert res1.currentRewardPointsBalance == 390
        assert res2.creditAccountId == '1'
        assert res2.currentRewardPointsBalance == 410
    }

    @Test
    void updateRewardsTest(){
        MockObjects mockObjects = new MockObjects()

        CreditCardProgram creditCardPro = mockObjects.getTestCreditCardProgram()
        creditCardPro.setScheduleOfCharges(null)

        CreditCardProgram savedCreditCardProgram = cardProgramRepository.save(creditCardPro)

        RewardsOffer offerObj = new RewardsOffer().tap {
            offerType = Offer.OfferType.FESTIVE_OFFER
            currencyConversionRatio = new Double(0.3)
            amountToSpendForRewardsInMultiple = new Integer(20)
            rewardPointsToBeGained = new Integer(50)
            targetConversionCurrency = Constants.Currency.INR
            creditCardProgram = savedCreditCardProgram
        }

        RewardsOffer off = rewardsOfferRepository.save(offerObj)

        Reward rewardObj = new Reward().tap {
            rewardBalance = 400
            rewardOnHold = 60
            offer = off
        }

        Transaction transaction = new Transaction().tap {
            transactionCurrency = Constants.Currency.INR
            transactionType = TransactionType.REWARDS_CREDIT
            transactionAmount = 50
            account = new CreditAccount().tap{
                id = '2'
                reward = rewardObj
            }
        }
        RewardsResponse rewardsResponse = rewardService.updateRewards(transaction)

        assert rewardsResponse.creditAccountId == '2'
        assert rewardsResponse.currentRewardPointsBalance == 500
    }
}
