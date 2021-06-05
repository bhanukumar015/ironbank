package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AccountService {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    CardRepository cardRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

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

    public Card createCard(Customer customer, CreditAccount creditAccount, CreditCardProgram cardProgram) {
        List<Card> existingOnes = getCards(creditAccount)
        if (existingOnes.size() > 0) {
            return existingOnes.get(0)
        }
        Card card = new Card()
        card.creditAccount = creditAccount
        card.cardProgram = cardProgram
        card.cardBin = cardProgram.cardBin
        card.cardExpiryMonth = 10
        card.cardExpiryYear = 2030
        card.lastFourDigits = String.format("%04d", System.currentTimeMillis() % 10000)
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
