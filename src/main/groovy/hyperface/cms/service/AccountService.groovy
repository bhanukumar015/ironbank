package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AccountService {

    @Autowired
    CardRepository cardRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    NiumCardService niumCardService

    public CreditAccount createCreditAccount(Customer customer, Constants.Currency currency, Double approvedCreditLimit) {
        CreditAccount creditAccount = new CreditAccount()
        creditAccount.currentBalance = 0
        creditAccount.approvedCreditLimit = approvedCreditLimit
        creditAccount.availableCreditLimit = approvedCreditLimit
        creditAccount.defaultCurrency = currency
        creditAccount.customer = customer
        creditAccountRepository.save(creditAccount)
        return creditAccount
    }

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
        card.enableDomesticTransactions = false
        card.enableNFC = false
        card.enableOnlineTransactions = false
        card.enableCashWithdrawal = false

        card.dailyTransactionLimit = cardProgram.defaultDailyTransactionLimit
        card.dailyCashWithdrawalLimit = cardProgram.defaultDailyCashWithdrawalLimit

        cardRepository.save(card)
        return card
    }
}
