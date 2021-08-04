package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.config.APIGatewayConfig
import hyperface.cms.domains.Client
import hyperface.cms.domains.ClientKey
import hyperface.cms.kong.AdminAPIGateway
import hyperface.cms.kong.dto.ConsumerObject
import hyperface.cms.kong.dto.PluginObject
import hyperface.cms.util.Utilities
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class ClientService {

    @Autowired
    APIGatewayConfig gatewayConfig

    @Autowired
    AdminAPIGateway adminAPIGateway

    ClientKey createClientKey(Client client) {
        ClientKey clientKey = new ClientKey()
        clientKey.client = client

        if (gatewayConfig.isServiceEnabled) {
            ConsumerObject consumer = new ConsumerObject(client.name)
            adminAPIGateway.createConsumer(consumer)

            PluginObject plugin = new PluginObject(Constants.KEY_AUTH_PLUGIN)
            clientKey.secretKey = adminAPIGateway.addPlugin(plugin, consumer.getUsername())
        } else {
            clientKey.secretKey = "secret_" + Utilities.generateUniqueReference(Constants.RANDOM_KEY_GENERATOR_LENGTH)
        }

        return clientKey
    }
}
