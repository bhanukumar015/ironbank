package hyperface.cms.repository

import hyperface.cms.domains.CardBin
import hyperface.cms.domains.CreditAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CreditAccountRepository extends CrudRepository<CreditAccount, String> {

}