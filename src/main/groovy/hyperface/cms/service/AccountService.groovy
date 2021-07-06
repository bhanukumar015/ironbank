package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AccountService {

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
}
