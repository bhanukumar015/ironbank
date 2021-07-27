package hyperface.cms.repository

import hyperface.cms.domains.ledger.TransactionLedger
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

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
}