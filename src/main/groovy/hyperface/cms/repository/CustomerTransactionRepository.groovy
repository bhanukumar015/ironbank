package hyperface.cms.repository

import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.batch.CurrencyConversion
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerTransactionRepository extends TransactionRepository<CustomerTransaction>, CrudRepository<CustomerTransaction, String> {
}