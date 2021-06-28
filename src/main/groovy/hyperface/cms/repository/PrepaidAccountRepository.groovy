package hyperface.cms.repository


import hyperface.cms.domains.PrepaidAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PrepaidAccountRepository extends AccountRepository<PrepaidAccount>, CrudRepository<PrepaidAccount, String> {

}