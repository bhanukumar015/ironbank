package hyperface.cms.service

import hyperface.cms.Constants.CardType
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CardChannelControlsRequest
import hyperface.cms.commands.CardLimitsRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.commands.CardLimitsRequest.CardLimit.TransactionLimitType
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
                    && (cardRequest.cardType == card.cardType)}
            if(existingCard){
                return Either.left(new GenericErrorResponse(reason: "Primary card of type ${cardRequest.cardType}" +
                        "already exists with cardId ${existingCard.id}"))
            }
        }

        if(cardRequest.cardType == CardType.Phygital){
            def physicalCardMetadata = niumCardService.createCard(cardRequest.tap {() ->
                    cardType = CardType.Physical}
                    , cardProgram, customer.switchMetadata)
            def virtualCardMetadata = niumCardService.createCard(cardRequest.tap {() ->
                    cardType = CardType.Virtual}
                    , cardProgram, customer.switchMetadata)
            Card physicalCard = createCardObject(physicalCardMetadata, CardType.Physical, cardProgram)
            Card virtualCard = createCardObject(virtualCardMetadata, CardType.Virtual, cardProgram)
            CardControl cardControl = createCardControlObject(cardRequest.cardType, cardProgram)
            physicalCard.phygitalDuoCardId = virtualCard.id
            virtualCard.phygitalDuoCardId = physicalCard.id
            cardControlRepository.save(cardControl)
            physicalCard.cardControl = virtualCard.cardControl = cardControl
            cardRepository.save(physicalCard)
            cardRepository.save(virtualCard)
            return Either.right(Arrays.asList(physicalCard, virtualCard))
        }
        else{
            def cardMetadata = niumCardService.createCard(cardRequest, cardProgram, customer.switchMetadata)
            Card card = createCardObject(cardMetadata, cardRequest.cardType, cardProgram)
            CardControl cardControl = createCardControlObject(card.cardType, cardProgram)
            cardControlRepository.save(cardControl)
            card.cardControl = cardControl
            cardRepository.save(card)
            return Either.right(Arrays.asList(card))
        }
    }

    private Card createCardObject(Map<String,Object> switchMetadata, CardType cardType, CreditCardProgram cardProgram){
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
        card.cardType = cardType
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
                value = cardProgram.defaultDailyTransactionLimit
            }
            dailyCashWithdrawalLimit = new TransactionLimit().tap{
                value = cardProgram.defaultDailyCashWithdrawalLimit
            }
            perTransactionLimit = new TransactionLimit()
            monthlyTransactionLimit = new TransactionLimit()
            lifetimeTransactionLimit = new TransactionLimit()
        }
    }

    Either<GenericErrorResponse,Boolean> setCardPin(SetCardPinRequest setCardPinRequest){
        // Valid pin has length of 4 and consists only digits
        if(!(setCardPinRequest.cardPin ==~ /^\d\d\d\d$/)){
            return Either.left(new GenericErrorResponse(reason: "Card pin length should be 4 and contain only digits"))
        }
        Optional<Card> cardOptional = cardRepository.findById(setCardPinRequest.cardId)
        if(!cardOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No card found with id " +
                    "${setCardPinRequest.cardId}"))
        }
        Card card = cardOptional.get()
        if(!card.physicallyIssued){
            throw new InvalidParameterException("Pin update is only allowed for physical cards")
        }
        Customer customer = card.creditAccount.customer
        return Either.right(niumCardService.setCardPin(setCardPinRequest, customer.switchMetadata, card.switchCardId))
    }

    Either<GenericErrorResponse,Card> invokeCardBlockAction(CardBlockActionRequest cardBlockActionRequest){
        Optional<Card> cardOptional = cardRepository.findById(cardBlockActionRequest.cardId)
        if(!cardOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No card found with id " +
                    "${cardBlockActionRequest.cardId}"))
        }
        Card card = cardOptional.get()
        if(card.hotlisted){
            return Either.left(new GenericErrorResponse(reason: "Block actions cannot be invoked on a hotlisted card"))
        }
        if(niumCardService.invokeCardAction(cardBlockActionRequest, card.creditAccount.customer.switchMetadata
                        ,card.switchCardId)){
            switch (cardBlockActionRequest.blockAction){
                case CardBlockActionRequest.BlockAction.TEMPORARYBLOCK:
                    card.isLocked = true
                    break
                case CardBlockActionRequest.BlockAction.UNBLOCK:
                    card.isLocked = false
                    break
                case CardBlockActionRequest.BlockAction.PERMANENTBLOCK:
                    card.hotlisted = true
                    break
                default:
                    throw new InvalidParameterException("Invalid block action!")
            }
            cardRepository.save(card)
        }
        return Either.right(card)
    }

    Either<GenericErrorResponse,Card> updateCardControls(CardChannelControlsRequest cardChannelControlsRequest){
        Optional<Card> cardOptional = cardRepository.findById(cardChannelControlsRequest.cardId)
        if(!cardOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No card found with card id " +
                    "${cardChannelControlsRequest.cardId}"))
        }
        Card card = cardOptional.get()

        if(card.hotlisted){
            throw new Exception("Channel controls cannot be invoked on a hotlisted card")
        }
        card.cardControl.enableOnlineTransactions = cardChannelControlsRequest.enableOnlineTransactions
        card.cardControl.enableOfflineTransactions = cardChannelControlsRequest.enableOfflineTransactions
        card.cardControl.enableOverseasTransactions = cardChannelControlsRequest.enableOverseasTransactions
        card.cardControl.enableCashWithdrawal = cardChannelControlsRequest.enableCashWithdrawl
        card.cardControl.enableMagStripe = cardChannelControlsRequest.enableMagStripe
        card.cardControl.enableNFC = cardChannelControlsRequest.enableNFC
        cardControlRepository.save(card.cardControl)
        return Either.right(card)
    }

    Either<GenericErrorResponse,Card> activateCard(String cardId){
        Optional<Card> cardOptional = cardRepository.findById(cardId)
        if(!cardOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No card found with id " +
                    "${cardId}"))
        }
        Card card = cardOptional.get()
        boolean response = niumCardService.activateCard(card)
        if(response){
            card.physicalCardActivated = card.physicallyIssued ? true : card.physicalCardActivated
            card.virtualCardActivated = card.virtuallyIssued ? true : card.virtualCardActivated
        }
        if(card.cardType == CardType.Virtual && card.secondaryPhygitalCardId != null
                && card.cardProgram.physicalCardActivation == CreditCardProgram.CardActivation.AUTO){
            Card secondaryCard = cardRepository.findById(card.secondaryPhygitalCardId).get()
            secondaryCard.physicalCardActivated = true
            cardRepository.save(secondaryCard)
        }
        cardRepository.save(card)

        return Either.right(card)
    }

    Either<GenericErrorResponse,Card> setCardLimits(CardLimitsRequest cardLimitsRequest){
        Optional<Card> cardOptional = cardRepository.findById(cardLimitsRequest.cardId)
        if(!cardOptional.isPresent()){
            return Either.left(new GenericErrorResponse(reason: "No card found with card id " +
                    "${cardLimitsRequest.cardId}"))
        }
        Card card = cardOptional.get()
        CardControl cardControl = card.cardControl

        for(def limit : cardLimitsRequest.cardLimits){
            switch(limit.type){
                case TransactionLimitType.PER_TRANSACTION_LIMIT:
                    cardControl.perTransactionLimit.value = limit.value.toDouble()
                    cardControl.perTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.perTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.DAILY_LIMIT:
                    cardControl.dailyTransactionLimit.value = limit.value.toDouble()
                    cardControl.dailyTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.dailyTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.MONTHLY_LIMIT:
                    cardControl.monthlyTransactionLimit.value = limit.value.toDouble()
                    cardControl.monthlyTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.monthlyTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.LIFETIME_LIMIT:
                    cardControl.lifetimeTransactionLimit.value = limit.value.toDouble()
                    cardControl.lifetimeTransactionLimit.isEnabled = limit.isEnabled
                    cardControl.lifetimeTransactionLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                case TransactionLimitType.DAILY_CASH_WITHDRAWAL_LIMIT:
                    cardControl.dailyCashWithdrawalLimit.value = limit.value.toDouble()
                    cardControl.dailyCashWithdrawalLimit.isEnabled = limit.isEnabled
                    cardControl.dailyCashWithdrawalLimit.additionalMarginPercentage = limit.additionalMarginPercentage
                    break
                default:
                    throw new InvalidParameterException("Invalid Limit Type!")
            }
        }
        cardControlRepository.save(cardControl)
        return Either.right(card)
    }

}
