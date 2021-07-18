package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.domains.Client
import hyperface.cms.domains.ClientKey
import hyperface.cms.util.Utilities
import org.springframework.stereotype.Service

@Service
@Slf4j
class ClientService {

    static ClientKey createClientKey(Client client) {
        //todo: may require to implement Kong Service here.
        ClientKey clientKey = new ClientKey()
        clientKey.secretKey = "secret_" + Utilities.generateUniqueReference(Constants.RANDOM_KEY_GENERATOR_LENGTH)
        clientKey.client = client
        return clientKey
    }
}
