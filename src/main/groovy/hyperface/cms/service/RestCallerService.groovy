package hyperface.cms.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.interceptors.RestTemplateErrorResponseHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


/** Service class to call REST services.*/
@Service
@Slf4j
class RestCallerService<T> {


    @Autowired
    ObjectMapper objectMapper

    @Autowired
    RestTemplateErrorResponseHandler defaultErrorResponseHandler

    ResponseEntity<T> call(String url, HttpMethod httpMethod, T body, Map<String, Object> params, Class<T> responseType, RestTemplateErrorResponseHandler customErrorResponseHandler) {
        log.info("invoking external rest api call to [${httpMethod}] - ${url}")

        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)

        if (params == null || params.isEmpty()) {
            params = new HashMap<>()
        }

        RestTemplate restTemplate = new RestTemplate()
        setErrorResponseHandler(restTemplate, customErrorResponseHandler)
        return restTemplate.exchange(url, httpMethod, new HttpEntity<Object>(body, httpHeaders), responseType, params)
    }

    private void setErrorResponseHandler(RestTemplate restTemplate, RestTemplateErrorResponseHandler errorHandler) {
        if (errorHandler == null) {
            restTemplate.setErrorHandler(defaultErrorResponseHandler);
        } else {
            restTemplate.setErrorHandler(errorHandler);
        }
    }

    Map processStringResponseToMap(String response) {
        return objectMapper.readValue(response, Map.class)
    }
}
