package hyperface.cms.repository

import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LedgerEntryRepository extends CrudRepository<LedgerEntry, Long> {

    @Query("SELECT le FROM LedgerEntry le WHERE le.account = ?1 and le.createdOn >= ?2 and le.createdOn < ?3")
    public List<LedgerEntry> findAllByCreditAccountInRange(CreditAccount creditAccount, Date from, Date to)
}