package hyperface.cms.kong

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.config.APIGatewayConfig
import hyperface.cms.kong.dto.ConsumerObject
import hyperface.cms.kong.dto.PluginObject
import hyperface.cms.service.RestCallerService
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
@Slf4j
class AdminAPIGateway {

    @Autowired
    APIGatewayConfig gatewayConfig

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    RestCallerService restCallerService

    private String prepareUrl(String endpoint) {
        return new StringBuilder()
                .append(gatewayConfig.getAdminURL())
                .append(endpoint)
    }

    void createConsumer(ConsumerObject consumerObject) {
        Map<String, String> headers = new HashMap<>()
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        HttpResponse<JsonNode> response = restCallerService.executeHttpPostRequestSync(prepareUrl(Constants.CONSUMERS_BASE_URL), headers, consumerObject)

        if (![HttpStatus.OK, HttpStatus.CREATED].contains(HttpStatus.valueOf(response.status))) {
            log.error("Failed to register client: ${consumerObject.getUsername()} as Consumer at Kong")

            Map responseMap = objectMapper.readValue(response.getBody().toString(), Map.class)
            String message = responseMap?.get("message") ?: response.getBody().toString()
            throw new ResponseStatusException(HttpStatus.valueOf(response.status), message)
        }
    }

    String addPlugin(PluginObject pluginObject, String consumer) {
        String endpoint = new StringBuilder()
                .append(Constants.CONSUMERS_BASE_URL)
                .append(Constants.PATH_SEPARATOR)
                .append(consumer)
                .append(Constants.PATH_SEPARATOR)
                .append(pluginObject.getName())

        Map<String, String> headers = new HashMap<>()

        HttpResponse<JsonNode> response = restCallerService.executeHttpPostRequestSync(prepareUrl(endpoint), headers, "")

        if ([HttpStatus.OK, HttpStatus.CREATED].contains(HttpStatus.valueOf(response.status))) {
            Map map = objectMapper.readValue(response.getBody().toString(), Map.class)
            return map.get("key")
        } else {
            Map responseMap = objectMapper.readValue(response.getBody().toString(), Map.class)
            String message = responseMap?.get("message") ?: response.getBody().toString()
            log.error("Failed to add plugin: ${pluginObject.getName()} to client: ${consumer} at Kong due to ${message}")

            log.info("Deleting registered client")
            endpoint = new StringBuilder()
                    .append(Constants.CONSUMERS_BASE_URL)
                    .append(Constants.PATH_SEPARATOR)
                    .append(consumer)
            HttpResponse<JsonNode> deleteResponse = restCallerService.executeHttpDeleteRequestSync(prepareUrl(endpoint), headers)

            if (![HttpStatus.NO_CONTENT].contains(HttpStatus.valueOf(deleteResponse.status))) {
                responseMap = objectMapper.readValue(deleteResponse.getBody().toString(), Map.class)
                message = responseMap?.get("message") ?: deleteResponse.getBody().toString()
                log.error("Failed to delete registered client: ${consumer} at Kong due to ${message}")

                throw new ResponseStatusException(HttpStatus.valueOf(deleteResponse.status), message)
            }
            throw new ResponseStatusException(HttpStatus.valueOf(response.status), message)
        }
    }
}
