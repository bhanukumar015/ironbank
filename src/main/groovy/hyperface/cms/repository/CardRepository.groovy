package hyperface.cms.repository

import hyperface.cms.domains.Card
import hyperface.cms.domains.CardBin
import hyperface.cms.domains.CardProgram
import hyperface.cms.domains.CreditAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository extends CrudRepository<Card, Long> {
    List<Card> findByCreditAccount(CreditAccount creditAccount)
    List<Card> findByCreditAccountAndCardProgram(CreditAccount creditAccount, CardProgram cardProgram)
}