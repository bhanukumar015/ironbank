package hyperface.cms.service.SwitchProviders.Nium.Utility

import groovy.util.logging.Slf4j
import hyperface.cms.config.SwitchProvidersConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
@Slf4j
class NiumRestUtils {

    @Autowired
    SwitchProvidersConfig switchProvidersConfig


    String prepareURL(String endpoint) {
        return new StringBuilder()
                .append(switchProvidersConfig.getNiumUrl())
                .append(endpoint)
                .toString()
    }

    Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>()
        headers.put('x-api-key', switchProvidersConfig.niumAPIKey)
        headers.put('x-client-name', switchProvidersConfig.niumClientName)
        headers.put(HttpHeaders.CONTENT_TYPE, 'application/json')
        headers.put('x-request-id', UUID.randomUUID().toString())
        log.info("Request to Nium with request id: ${headers.get("x-request-id")} is passed")
        return headers
    }
}
