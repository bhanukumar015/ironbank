package hyperface.cms.repository

import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.ledger.TransactionLedger
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

import java.time.ZonedDateTime

@Repository
interface TransactionLedgerRepository extends CrudRepository<TransactionLedger, String> {
    /**
     * Method to retrieve all ledger entries, related to a single
     * TxnRefId, that corresponds to a Transaction in the DB.
     *
     * @param txnRefId
     * @return List of TransactionLedger, for the given txnRefId
     */
    @Query("SELECT tl FROM TransactionLedger tl WHERE tl.transaction.id = :txnRefId")
    List<TransactionLedger> findAllByTxnRefId(@Param("txnRefId") String txnRefId)

    @Query("SELECT tl from TransactionLedger tl WHERE tl.creditAccount = :account and (tl.transaction.txnDate >= :from AND tl.transaction.txnDate <= :to) ORDER BY tl.transaction.txnDate DESC")
    List<TransactionLedger> findByAccountInRange(@Param("account") CreditAccount account
                                                 , @Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to)

}