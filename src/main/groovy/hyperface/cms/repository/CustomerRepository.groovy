package hyperface.cms.repository

import hyperface.cms.domains.Customer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository extends CrudRepository<Customer, Long> {

}