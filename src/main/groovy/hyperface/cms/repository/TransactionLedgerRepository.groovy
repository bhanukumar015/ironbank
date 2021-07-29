package hyperface.cms.repository

import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.ledger.TransactionLedger
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

    @Query("SELECT tl FROM TransactionLedger tl WHERE tl.creditAccount = ?1 AND tl.postingDate >= ?2 AND tl.postingDate < ?3 ORDER BY tl.postingDate ASC")
    List<TransactionLedger> findAllByCreditAccountInRange(CreditAccount creditAccount, ZonedDateTime from, ZonedDateTime to)
}