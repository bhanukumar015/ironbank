package hyperface.cms.repository

import hyperface.cms.domains.CardStatement
import org.springframework.data.repository.CrudRepository

interface CardStatementRepository extends CrudRepository<CardStatement, String> {

}