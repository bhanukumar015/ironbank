package hyperface.cms.service


import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CardChannelControlsRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.security.InvalidParameterException

@Service
class CardService {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    CardRepository cardRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    NiumCardService niumCardService

    public List<Card> getCards(CreditAccount creditAccount) {
        List<Card> cards = cardRepository.findByCreditAccount(creditAccount)
        return cards
    }

    public Card createCard(CreateCardRequest cardRequest) {
        CreditAccount creditAccount = creditAccountRepository.findById(cardRequest.creditAccountId)
                .orElseThrow(() -> new IllegalArgumentException("No credit account found " +
                        "with the given Id ${cardRequest.creditAccountId}"))
        CreditCardProgram cardProgram = cardProgramRepository.findById(cardRequest.cardProgramId as Long)
                .orElseThrow(() -> new IllegalArgumentException("No card program found with " +
                        "the given Id ${cardRequest.cardProgramId}"))
        List<Card> existingOnes = getCards(creditAccount)
        if (existingOnes.size() > 0) {
            return existingOnes.get(0)
        }

        def switchCardMetadata = niumCardService.createCard(cardRequest, cardProgram)

        Card card = new Card()
        card.creditAccount = creditAccount
        card.cardProgram = cardProgram
        card.cardBin = cardProgram.cardBin
        card.cardExpiryMonth = 10
        card.cardExpiryYear = 2030
        card.switchCardId = switchCardMetadata.get('switchCardId').toString()
        card.lastFourDigits = switchCardMetadata.get('maskedCardNumber').toString()[-4..-1]
        card.physicallyIssued = false
        card.virtuallyIssued = true
        card.virtualCardActivatedByCustomer = false
        card.physicalCardActivatedByCustomer = false
        card.cardSuspendedByCustomer = false
        card.enableOverseasTransactions = false
        card.enableOfflineTransactions = false
        card.enableNFC = false
        card.enableOnlineTransactions = false
        card.enableCashWithdrawal = false
        card.enableMagStripe = false

        card.dailyTransactionLimit = cardProgram.defaultDailyTransactionLimit
        card.dailyCashWithdrawalLimit = cardProgram.defaultDailyCashWithdrawalLimit

        cardRepository.save(card)
        return card
    }

    public boolean setCardPin(SetCardPinRequest setCardPinRequest){
        // Valid pin has length of 4 and consists only digits
        if(!(setCardPinRequest.cardPin ==~ /^\d\d\d\d$/)){
            throw new InvalidParameterException("Card pin length should be 4")
        }
        Card card = cardRepository.findById(setCardPinRequest.cardId)
                .orElseThrow(() -> new IllegalArgumentException("No card found with the card " +
                        "id ${setCardPinRequest.cardId}"))
        if(!card.physicallyIssued){
            throw new InvalidParameterException("Pin update is only allowed for physical cards")
        }
        Customer customer = card.creditAccount.customer
        return niumCardService.setCardPin(setCardPinRequest, customer.switchMetadata, card.switchCardId)
    }

    public Card invokeCardBlockAction(CardBlockActionRequest cardBlockActionRequest){
        Card card = cardRepository.findById(cardBlockActionRequest.cardId)
                .orElseThrow(() -> new InvalidParameterException("No card found with the card " +
                        "id ${cardBlockActionRequest.cardId}"))
        if(card.hotlisted){
            throw new Exception("Block actions cannot be invoked on a hotlisted card")
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
        return card
    }

    public Card updateCardControls(CardChannelControlsRequest cardChannelControlsRequest){
        Card card = cardRepository.findById(cardChannelControlsRequest.cardId)
                .orElseThrow(() -> new IllegalArgumentException("No card found with the card " +
                        "id ${cardChannelControlsRequest.cardId}"))
        if(card.hotlisted){
            throw new Exception("Channel controls cannot be invoked on a hotlisted card")
        }
        card.enableOnlineTransactions = cardChannelControlsRequest.enableOnlineTransactions
        card.enableOfflineTransactions = cardChannelControlsRequest.enableOfflineTransactions
        card.enableOverseasTransactions = cardChannelControlsRequest.enableOverseasTransactions
        card.enableCashWithdrawal = cardChannelControlsRequest.enableCashWithdrawl
        card.enableMagStripe = cardChannelControlsRequest.enableMagStripe
        card.enableNFC = cardChannelControlsRequest.enableNFC
        cardRepository.save(card)
        return card
    }

}
