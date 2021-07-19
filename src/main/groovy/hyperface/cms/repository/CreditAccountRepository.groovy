package hyperface.cms.repository


import hyperface.cms.domains.CreditAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

import java.time.ZonedDateTime

@Repository
interface CreditAccountRepository extends AccountRepository<CreditAccount>, JpaRepository<CreditAccount, String> {

    @Query(nativeQuery = true, value= "select * from credit_account where current_billing_end_date >= ?1 and current_billing_end_date < ?2")
    List<CreditAccount> findAllByCurrentBillingEndDateGreaterThanEqualAndCurrentBillingEndDateLessThan(ZonedDateTime start, ZonedDateTime end)
}