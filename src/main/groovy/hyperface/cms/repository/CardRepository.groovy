package hyperface.cms.repository

import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository extends CrudRepository<Card, String> {
    List<Card> findByCreditAccount(CreditAccount creditAccount)

    List<Card> findByCreditAccountAndCardProgram(CreditAccount creditAccount, CreditCardProgram cardProgram)
}