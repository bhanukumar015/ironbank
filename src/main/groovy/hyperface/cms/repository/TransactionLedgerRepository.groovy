package hyperface.cms.repository

import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.TransactionType
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

    @Query("SELECT tl FROM TransactionLedger tl WHERE tl.account = ?1 and tl.postingDate >= ?2 and tl.postingDate <= ?3 order by tl.postingDate desc")
    List<TransactionLedger> findAllByCreditAccountInRange(CreditAccount creditAccount, ZonedDateTime from, ZonedDateTime to)

    @Query("SELECT SUM(tl.transactionAmount) FROM TransactionLedger tl WHERE tl.account = ?1 and tl.transactionType = ?2 and tl.postingDate >= ?3  and tl.postingDate <= ?4")
    Double getSumByCreditAccountAndTxnTypeInRange(CreditAccount creditAccount, LedgerTransactionType type, ZonedDateTime from, ZonedDateTime to)

    @Query("SELECT SUM(tl.transactionAmount) FROM TransactionLedger tl WHERE tl.account = ?1 and tl.postingDate >= ?2  and tl.postingDate <= ?3")
    Double getSumByCreditAccountInRange(CreditAccount creditAccount, ZonedDateTime from, ZonedDateTime to)

}