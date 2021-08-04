package hyperface.cms.repository

import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTransaction
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import java.time.ZonedDateTime

@Repository
interface CustomerTransactionRepository extends TransactionRepository<CustomerTransaction>, CrudRepository<CustomerTransaction, String> {

    @Query("SELECT ct from CustomerTransaction ct WHERE ct.account = ?1 AND (ct.txnDate >= ?2 AND ct.txnDate <= ?3) ORDER BY ct.txnDate DESC")
    List<CustomerTransaction> findAuthTransactionsByAccountInRange(CreditAccount account
                , ZonedDateTime from, ZonedDateTime to)
}