package hyperface.cms.repository

import hyperface.cms.domains.Client
import org.springframework.data.repository.CrudRepository

interface ClientRepository extends CrudRepository<Client, String> {
}
