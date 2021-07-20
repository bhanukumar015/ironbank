package hyperface.cms.service

import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.util.CardProgramManagement
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AccountService {

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramManagement cardProgramManagement

    public Either<String, CreditAccount> createCreditAccount(Customer customer, CreditCardProgram cardProgram
                                                     , Double approvedCreditLimit) {
        if (!cardProgram.isActive) {
            String errorMessage = "Cannot create accounts as card program" + ((cardProgram.disableLevel == CreditCardProgram.DisableLevel.MANUAL)
                            ? " was manually disabled"
                            : "'s ${cardProgram.disableLevel.toString().toLowerCase()} limit exceeded")
            return Either.left(errorMessage)
        }

        CreditAccount creditAccount = new CreditAccount()
        creditAccount.currentBalance = 0
        creditAccount.approvedCreditLimit = approvedCreditLimit
        creditAccount.availableCreditLimit = approvedCreditLimit
        creditAccount.defaultCurrency = cardProgram.baseCurrency
        creditAccount.customer = customer
        creditAccountRepository.save(creditAccount)
        cardProgramManagement.updateCardProgramCounts(cardProgram)
        return Either.right(creditAccount)
    }
}
