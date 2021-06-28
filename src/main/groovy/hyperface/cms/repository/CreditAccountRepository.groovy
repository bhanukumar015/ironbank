package hyperface.cms.repository


import hyperface.cms.domains.CreditAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CreditAccountRepository extends AccountRepository<CreditAccount>, CrudRepository<CreditAccount, String> {

}