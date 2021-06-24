package hyperface.cms.repository


import hyperface.cms.domains.SystemTransaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SystemTransactionRepository extends TransactionRepository<SystemTransaction>, CrudRepository<SystemTransaction, String> {

}