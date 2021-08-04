package hyperface.cms.kong

import hyperface.cms.Constants
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.config.APIGatewayConfig
import hyperface.cms.kong.dto.ConsumerObject
import hyperface.cms.kong.dto.PluginObject
import hyperface.cms.service.RestCallerService
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>()
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        return headers
    }

    void createConsumer(ConsumerObject consumerObject) {
        String response = restCallerService.executeHttpPostRequestSync(prepareUrl(Constants.CONSUMERS_BASE_URL), getHeaders(), consumerObject.toString())
    }

    String addPlugin(PluginObject pluginObject, String consumer) {
        String api = new StringBuilder()
                .append(Constants.CONSUMERS_BASE_URL)
                .append(Constants.PATH_SEPARATOR)
                .append(consumer)
                .append(Constants.PATH_SEPARATOR)
                .append(pluginObject.getName())

        String response = restCallerService.executeHttpPostRequestSync(prepareUrl(api), getHeaders(), "")
        Map map = restCallerService.processStringResponseToMap(response)
        map.get("key")
    }
}
