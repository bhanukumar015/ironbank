package hyperface.cms.repository

import hyperface.cms.domains.CustomerTxn
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerTxnRepository extends CrudRepository<CustomerTxn, Long> {

}