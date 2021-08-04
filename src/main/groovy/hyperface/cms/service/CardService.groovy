package hyperface.cms.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.Constants.CardType
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CardChannelControlsRequest
import hyperface.cms.commands.CardLimitsRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.commands.CardLimit.TransactionLimitType
import hyperface.cms.domains.Card
import hyperface.cms.domains.CardControl
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.domains.TransactionLimit
import hyperface.cms.repository.CardControlRepository
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCardService
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.security.InvalidParameterException

@Service
@Slf4j
class CardService {

    @Autowired
    CardRepository cardRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    NiumCardService niumCardService

    @Autowired
    CardControlRepository cardControlRepository

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    FeeService feeService

    Either<GenericErrorResponse,List<Card>> createCard(CreateCardRequest cardRequest) {
        Optional<CreditAccount> creditAccountOptional = creditAccountRepository.findById(cardRequest.creditAccountId)
        if(!creditAccountOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No credit account found with id " +
                    "${cardRequest.creditAccountId}"))
        }
        CreditAccount creditAccount = creditAccountOptional.get()
        Optional<CreditCardProgram> cardProgramOptional = cardProgramRepository.findById(cardRequest.cardProgramId)
        if(!cardProgramOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No card program found with id " +
                    "${cardRequest.cardProgramId}"))
        }
        CreditCardProgram cardProgram = cardProgramOptional.get()
        Optional<Customer> customerOptional = customerRepository.findById(cardRequest.customerId)
        if(!customerOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No customer found with id ${cardRequest.customerId}"))
        }
        Customer customer = customerOptional.get()
        if(!cardRequest.isAddOn){
            Card existingCard = creditAccount.cards.find {card -> card.isPrimaryCard
                    && (cardRequest.cardType == card.cardType.toString())}
            if(existingCard){
                return Either.left(new GenericErrorResponse(reason: "Primary card of type ${cardRequest.cardType}" +
                        "already exists with cardId ${existingCard.id}"))
            }
        }
        else{
            Optional<Card> primaryCardOptional = cardRepository.findById(cardRequest.primaryCardId)
            if(!primaryCardOptional.isPresent()){
                return Either.left(new GenericErrorResponse(reason: "No primary card found with id ${cardRequest.primaryCardId}" +
                        "for addOn card request"))
            }
            Card primaryCard = primaryCardOptional.get()
            feeService.createAddOnCardFeeEntry(primaryCard)
        }

        if(cardRequest.cardType == CardType.Phygital.toString()){
            def physicalCardMetadata = niumCardService.createCard(cardRequest.tap {() ->
                    cardType = CardType.Physical.toString()}
                    , cardProgram, customer.switchMetadata)
            def virtualCardMetadata = niumCardService.createCard(cardRequest.tap {() ->
                    cardType = CardType.Virtual.toString()}
                    , cardProgram, customer.switchMetadata)
            Card physicalCard = createCardObject(cardRequest.isAddOn, physicalCardMetadata, CardType.Physical, cardProgram)
            Card virtualCard = createCardObject(cardRequest.isAddOn, virtualCardMetadata, CardType.Virtual, cardProgram)
            CardControl cardControl = createCardControlObject(CardType.valueOf(cardRequest.cardType), cardProgram)
            physicalCard.phygitalDuoCardId = virtualCard.id
            virtualCard.phygitalDuoCardId = physicalCard.id
            cardControlRepository.save(cardControl)
            physicalCard.cardControl = virtualCard.cardControl = cardControl
            physicalCard.creditAccount = virtualCard.creditAccount = creditAccount
            cardRepository.save(physicalCard)
            cardRepository.save(virtualCard)
            creditAccount.cards.add(physicalCard)
            creditAccount.cards.add(virtualCard)
            creditAccountRepository.save(creditAccount)
            return Either.right(Arrays.asList(physicalCard, virtualCard))
        }
        else{
            def cardMetadata = niumCardService.createCard(cardRequest, cardProgram, customer.switchMetadata)
            Card card = createCardObject(cardRequest.isAddOn, cardMetadata, CardType.valueOf(cardRequest.cardType), cardProgram)
            CardControl cardControl = createCardControlObject(card.cardType, cardProgram)
            cardControlRepository.save(cardControl)
            card.cardControl = cardControl
            card.creditAccount = creditAccount
            log.info(new ObjectMapper().writeValueAsString(card))
            cardRepository.save(card)
            creditAccount.cards.add(card)
            creditAccountRepository.save(creditAccount)
            return Either.right(Arrays.asList(card))
        }
    }

    private Card createCardObject(Boolean isAddon, Map<String,Object> switchMetadata, CardType cardType, CreditCardProgram cardProgram){
        Card card = new Card()
        card.cardProgram = cardProgram
        card.cardBin = cardProgram.cardBin
        card.cardExpiryMonth = 10
        card.cardExpiryYear = 2030
        card.switchCardId = switchMetadata.get('switchCardId').toString()
        card.lastFourDigits = switchMetadata.get('maskedCardNumber').toString()[-4..-1]
        card.physicallyIssued = (cardType == CardType.Physical)
        card.virtuallyIssued = (cardType == CardType.Virtual)
        card.virtualCardActivated = (cardType != CardType.Physical)
                ? cardProgram.virtualCardActivation == CreditCardProgram.CardActivation.AUTO
                : false
        card.physicalCardActivated = false
        card.isPrimaryCard = !isAddon
        card.cardType = cardType
        card.isFirstRepaymentDone = false
        card.isFirstPurchaseDone = false
        log.info(new ObjectMapper().writeValueAsString(card))
        return card
    }

    private CardControl createCardControlObject(CardType cardType, CreditCardProgram cardProgram){
        return new CardControl().tap{
            cardSuspendedByCustomer = false
            enableOverseasTransactions = false
            enableOfflineTransactions = false
            enableNFC = false
            enableOnlineTransactions = (cardType != CardType.Physical
                    && cardProgram.virtualCardActivation == CreditCardProgram.CardActivation.AUTO)
            enableCashWithdrawal = false
            enableMagStripe = false
            dailyTransactionLimit = new TransactionLimit().tap{
                limit = cardProgram.defaultDailyTransactionLimit
            }
            dailyCashWithdrawalLimit = new TransactionLimit().tap{
                limit = cardProgram.defaultDailyCashWithdrawalLimit
            }
            onlineTransactionLimit = new TransactionLimit().tap{
                limit = cardProgram.defaultOnlineTransactionLimit
            }
            offlineTransactionLimit = new TransactionLimit().tap{
                limit = cardProgram.defaultOfflineTransactionLimit
            }
            cashWithdrawalLimit = new TransactionLimit().tap{
                limit = cardProgram.defaultCashWithdrawalLimit
            }
            monthlyTransactionLimit = new TransactionLimit().tap{
                limit = cardProgram.defaultMonthlyTransactionLimit
            }
            lifetimeTransactionLimit = new TransactionLimit().tap{
                limit = cardProgram.defaultLifetimeTransactionLimit
            }
        }
    }

    Either<GenericErrorResponse,String> setCardPin(SetCardPinRequest request){
        if(!request.card.physicallyIssued){
            String errorMessage = "Pin update is only allowed for physical cards"
            log.error("Pin update failed with error: ${errorMessage}")
            return Either.left(new GenericErrorResponse(reason: errorMessage))
        }
        Customer customer = request.card.creditAccount.customer
        return niumCardService.setCardPin(request, customer.switchMetadata)
    }

    Either<GenericErrorResponse, String> invokeCardBlockAction(CardBlockActionRequest req){
        if(req.card.hotlisted){
            return Either.left(new GenericErrorResponse(reason: "Block actions cannot be invoked on a hotlisted card"))
        }
        def response = niumCardService.invokeCardAction(req)
        if(response.isRight()){
            switch (req.blockAction){
                case CardBlockActionRequest.BlockAction.TEMPORARYBLOCK.toString():
                    req.card.isLocked = true
                    break
                case CardBlockActionRequest.BlockAction.UNBLOCK.toString():
                    req.card.isLocked = false
                    break
                case CardBlockActionRequest.BlockAction.PERMANENTBLOCK.toString():
                    req.card.hotlisted = true
                    break
                default:
                    throw new InvalidParameterException("Invalid block action!")
            }
            cardRepository.save(req.card)
            return Either.right(response.right().get())
        }
        return Either.left(response.left().get().reason)
    }

    Either<GenericErrorResponse,Card> updateCardControls(CardChannelControlsRequest req){
        if(req.card.hotlisted){
            throw new Exception("Channel controls cannot be invoked on a hotlisted card")
        }
        CardControl cardControl = req.card.cardControl
        cardControl.enableOnlineTransactions = req.enableOnlineTransactions ?: cardControl.enableOnlineTransactions
        cardControl.enableOfflineTransactions = req.enableOfflineTransactions ?: cardControl.enableOfflineTransactions
        cardControl.enableOverseasTransactions = req.enableOverseasTransactions ?: cardControl.enableOverseasTransactions
        cardControl.enableCashWithdrawal = req.enableCashWithdrawal ?: cardControl.enableCashWithdrawal
        cardControl.enableMagStripe = req.enableMagStripe ?: cardControl.enableMagStripe
        cardControl.enableNFC = req.enableNFC ?: cardControl.enableNFC
        cardControlRepository.save(req.card.cardControl)
        return Either.right(req.card)
    }

    Either<GenericErrorResponse,String> activateCard(String cardId){
        Optional<Card> cardOptional = cardRepository.findById(cardId)
        if(!cardOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No card found with id " +
                    "${cardId}"))
        }
        Card card = cardOptional.get()
        def response = niumCardService.activateCard(card)
        if(response.isRight()){
            card.physicalCardActivated = card.physicallyIssued ? true : card.physicalCardActivated
            card.virtualCardActivated = card.virtuallyIssued ? true : card.virtualCardActivated
            cardRepository.save(card)
        }
        if(card.cardType == CardType.Virtual && card.phygitalDuoCardId != null
                && card.cardProgram.physicalCardActivation == CreditCardProgram.CardActivation.AUTO){
            Card secondaryCard = cardRepository.findById(card.phygitalDuoCardId).get()
            secondaryCard.physicalCardActivated = true
            cardRepository.save(secondaryCard)
        }
        return response
    }

    Either<GenericErrorResponse,Card> setCardLimits(CardLimitsRequest req){
        CardControl cardControl = req.card.cardControl

        for(def limit : req.cardLimits){
            switch(limit.type){
                case TransactionLimitType.ONLINE_TRANSACTION_LIMIT.toString():
                    cardControl.onlineTransactionLimit.limit = limit.value.toDouble()
                    cardControl.onlineTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.onlineTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.OFFLINE_TRANSACTION_LIMIT.toString():
                    cardControl.offlineTransactionLimit.limit = limit.value.toDouble()
                    cardControl.offlineTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.offlineTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.CASH_WITHDRAWAL_LIMIT.toString():
                    cardControl.cashWithdrawalLimit.limit = limit.value.toDouble()
                    cardControl.cashWithdrawalLimit.isEnabled = limit.isEnabled
                    cardControl.cashWithdrawalLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.DAILY_LIMIT.toString():
                    cardControl.dailyTransactionLimit.limit = limit.value.toDouble()
                    cardControl.dailyTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.dailyTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.MONTHLY_LIMIT.toString():
                    cardControl.monthlyTransactionLimit.limit = limit.value.toDouble()
                    cardControl.monthlyTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.monthlyTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.LIFETIME_LIMIT.toString():
                    cardControl.lifetimeTransactionLimit.limit = limit.value.toDouble()
                    cardControl.lifetimeTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.lifetimeTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.DAILY_CASH_WITHDRAWAL_LIMIT.toString():
                    cardControl.dailyCashWithdrawalLimit.limit = limit.value.toDouble()
                    cardControl.dailyCashWithdrawalLimit.isEnabled = limit.isEnabled
                    cardControl.dailyCashWithdrawalLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                default:
                    return Either.left(new GenericErrorResponse(reason: "Invalid limit type"))
            }
        }
        cardControlRepository.save(cardControl)
        return Either.right(req.card)
    }

}
