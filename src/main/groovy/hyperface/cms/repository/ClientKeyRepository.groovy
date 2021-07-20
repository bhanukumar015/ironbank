package hyperface.cms.repository

import hyperface.cms.domains.ClientKey
import org.springframework.data.repository.CrudRepository

interface ClientKeyRepository extends CrudRepository<ClientKey, String> {

    ClientKey findByClientId(String clientId);
}