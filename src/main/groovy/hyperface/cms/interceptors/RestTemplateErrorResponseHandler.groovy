package hyperface.cms.interceptors

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.server.ResponseStatusException

import java.nio.charset.Charset

/**
 * Abstract Response Error Handler for RestCallerService class
 */
@Component
@Slf4j
class RestTemplateErrorResponseHandler implements ResponseErrorHandler {

    @Autowired
    ObjectMapper objectMapper

    @Override
    boolean hasError(ClientHttpResponse response) throws IOException {
        return (response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR ||
                response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR)
    }

    @Override
    void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = null

        try {
            responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())
        } catch (IOException e) {
            log.error(e.getMessage(), e)
            throw new RuntimeException(e)
        }

        Map responseMap = objectMapper.readValue(responseBody, Map.class)

        if (response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            log.error("[SERVER ERROR] - ${responseMap.get("message").toString()}")
            throw new ResponseStatusException(response.getStatusCode(), responseMap.get("message").toString())
        } else if (response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            //todo: may require to log or handle different error scenarios here
            log.error("[CLIENT ERROR] - ${responseMap.get("message").toString()}")
            throw new ResponseStatusException(response.getStatusCode(), responseMap.get("message").toString())
        }
    }
}
