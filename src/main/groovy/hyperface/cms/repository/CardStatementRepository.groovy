package hyperface.cms.repository
import hyperface.cms.domains.CardStatement
import hyperface.cms.domains.CreditAccount
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

import java.time.ZonedDateTime

interface CardStatementRepository extends CrudRepository<CardStatement, String> {

    @Query("SELECT cs from CardStatement cs WHERE cs.creditAccount = ?1 AND (cs.generatedOn >= ?2 AND cs.generatedOn <= ?3)")
    List<CardStatement> findByAccountInRange(CreditAccount account, ZonedDateTime from, ZonedDateTime to)

    CardStatement findTopByCreditAccountOrderByGeneratedOnDesc(CreditAccount account)
}