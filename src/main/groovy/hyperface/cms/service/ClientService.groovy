package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.domains.Client
import hyperface.cms.domains.ClientKey
import hyperface.cms.repository.ClientKeyRepository
import hyperface.cms.repository.ClientRepository
import hyperface.cms.util.Utilities
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
@Slf4j
@Transactional
class ClientService {

    @Autowired
    ClientRepository clientRepository

    @Autowired
    ClientKeyRepository clientKeyRepository

    static ClientKey createClientKey(Client client) {
        //todo: may require to implement Kong Service here.
        ClientKey clientKey = new ClientKey()
        clientKey.secretKey = "secret_" + Utilities.generateUniqueReference(Constants.RANDOM_KEY_GENERATOR_LENGTH)
        clientKey.client = client
        return clientKey
    }

    void removeClient(String clientId) {
        ClientKey clientKey = clientKeyRepository.findByClientId(clientId)
        clientKeyRepository.delete(clientKey)
        clientRepository.deleteById(clientId)
    }
}
