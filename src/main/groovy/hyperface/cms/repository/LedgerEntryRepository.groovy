package hyperface.cms.repository

import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LedgerEntryRepository extends CrudRepository<LedgerEntry, Long> {

}