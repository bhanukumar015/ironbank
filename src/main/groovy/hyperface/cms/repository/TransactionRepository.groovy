package hyperface.cms.repository

import hyperface.cms.domains.Transaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository extends CrudRepository<Transaction, String> {

}