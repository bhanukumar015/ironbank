package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.config.APIGatewayConfig
import hyperface.cms.domains.Client
import hyperface.cms.domains.ClientKey
import hyperface.cms.kong.AdminAPIGateway
import hyperface.cms.kong.dto.ConsumerObject
import hyperface.cms.kong.dto.PluginObject
import hyperface.cms.repository.ClientKeyRepository
import hyperface.cms.repository.ClientRepository
import hyperface.cms.util.Utilities
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
@Slf4j
class ClientService {

    @Autowired
    ClientRepository clientRepository

    @Autowired
    ClientKeyRepository clientKeyRepository

    @Autowired
    APIGatewayConfig gatewayConfig

    @Autowired
    AdminAPIGateway adminAPIGateway

    ClientKey createClientKey(Client client) {
        ClientKey clientKey = new ClientKey()
        clientKey.client = client

        if (gatewayConfig.isServiceEnabled) {
            ConsumerObject consumer = new ConsumerObject(client.name)

            Either<GenericErrorResponse, Void> createConsumerResult = adminAPIGateway.createConsumer(consumer)

            if (createConsumerResult.isLeft()) {
                String reason = createConsumerResult.left().get().getReason()
                log.error("Failed to add consumer at kong due to ${reason}")
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, reason)
            }

            PluginObject plugin = new PluginObject(Constants.KEY_AUTH_PLUGIN)

            Either<GenericErrorResponse, String> addPluginResult = adminAPIGateway.addPlugin(plugin, consumer.getUsername())
            if (addPluginResult.isLeft()) {
                String reason = addPluginResult.left().get().getReason()
                log.error("Failed to add plugin ${Constants.KEY_AUTH_PLUGIN} to client: ${client.getId()} at kong due to ${reason}")
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, reason)
            }

            clientKey.secretKey = addPluginResult.right().get()
        } else {
            clientKey.secretKey = "secret_" + Utilities.generateUniqueReference(Constants.RANDOM_KEY_GENERATOR_LENGTH)
        }

        return clientKey
    }
}
