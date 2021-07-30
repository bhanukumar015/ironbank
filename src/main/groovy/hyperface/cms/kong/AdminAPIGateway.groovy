package hyperface.cms.kong

import hyperface.cms.Constants
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.config.APIGatewayConfig
import hyperface.cms.kong.dto.ConsumerObject
import hyperface.cms.kong.dto.PluginObject
import hyperface.cms.service.RestCallerService
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap

@Service
class AdminAPIGateway {

    @Autowired
    APIGatewayConfig gatewayConfig

    @Autowired
    RestCallerService restCallerService

    private String prepareUrl(String api) {
        return new StringBuilder()
                .append(gatewayConfig.getAdminURL())
                .append(api)
    }

    Either<GenericErrorResponse, Void> createConsumer(ConsumerObject consumerObject) {
        ResponseEntity<String> response = restCallerService.call(prepareUrl(Constants.CONSUMERS_BASE_URL), HttpMethod.POST, consumerObject, null, String.class, null)
        if ([HttpStatus.OK, HttpStatus.CREATED].contains(response.statusCode)) {
            return Either.right()
        } else {
            return Either.left(new GenericErrorResponse(reason: "Error occurred while creating consumer"))
        }
    }

    Either<GenericErrorResponse, String> addPlugin(PluginObject pluginObject, String consumer) {
        String api = new StringBuilder()
                .append(Constants.CONSUMERS_BASE_URL)
                .append(Constants.PATH_SEPARATOR)
                .append(consumer)
                .append(Constants.PATH_SEPARATOR)
                .append(pluginObject.getName())

        ResponseEntity<String> response = restCallerService.call(prepareUrl(api), HttpMethod.POST, new LinkedMultiValueMap<>(), null, String.class, null)

        if ([HttpStatus.OK, HttpStatus.CREATED].contains(response.statusCode)) {
            Map map = restCallerService.processStringResponseToMap(response.getBody().toString())
            String key = map.get("key")
            return Either.right(key)
        } else {
            return Either.left(new GenericErrorResponse(reason: "Error occurred while creating consumer"))
        }
    }
}
