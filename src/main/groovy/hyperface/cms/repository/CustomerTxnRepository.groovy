package hyperface.cms.repository

import hyperface.cms.domains.Account
import hyperface.cms.domains.Card
import hyperface.cms.domains.CustomerTxn
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerTxnRepository extends CrudRepository<CustomerTxn, Long> {
    CustomerTxn findByRetrievalReferenceNumber(String retrievalReferenceNumber)

    @Query("select ct from CustomerTxn ct where ct.card = ?1 and retrievalReferenceNumber = ?2 and txnType = 'Authorize'")
    CustomerTxn findAuthTxnByCardAndRRN(Card card, String rrn)

    List<CustomerTxn> findByCard(Card card)

    @Query("select ct from CustomerTxn ct where ct.account = ?1 and createdOn >= ?2 and createdOn < ?3")
    List<CustomerTxn> findAllByAccountInRange(Account account, Date from, Date to)
}