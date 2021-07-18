package hyperface.cms.repository

import hyperface.cms.domains.CustomerTransaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerTransactionRepository extends TransactionRepository<CustomerTransaction>, CrudRepository<CustomerTransaction, String> {
}