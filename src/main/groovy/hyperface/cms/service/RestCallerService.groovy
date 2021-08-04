package hyperface.cms.service

import groovy.util.logging.Slf4j
import kong.unirest.Callback
import kong.unirest.HttpResponse
import kong.unirest.HttpStatus
import kong.unirest.JsonNode
import kong.unirest.Unirest
import kong.unirest.UnirestException
import org.apache.commons.lang3.ObjectUtils
import org.springframework.stereotype.Service

/** Service class to call REST services.*/
@Service
@Slf4j
class RestCallerService<T> {

    void executeHttpPostRequestAsync(String url, Map<String, String> headers, String requestBody, Callback<JsonNode> callback) {
        log.info("Invoking external Async REST API call to [POST] - ${url}")

        try {
            Unirest.post(url)
                    .headers(headers)
                    .body(requestBody)
                    .asJsonAsync(callback)
        }
        catch (UnirestException ex) {
            log.info "Post request to ${url} failed with message ${ex.message}"
            throw new Exception("Post request to ${url} failed with message ${ex.message}")
        }
    }

    String executeHttpPostRequestSync(String url, Map<String, String> headers, String requestBody, int retries) {
        log.info("Invoking external Sync REST API call to [POST] - ${url}")

        try {
            String retryResponse = null
            HttpResponse<JsonNode> response = Unirest.post(url)
                    .headers(headers)
                    .body(requestBody)
                    .asJson()
                    .ifSuccess(response -> {
                        log.info "POST request to ${url} is success"
                    })
                    .ifFailure(response -> {
                        if (retries > 0) {
                            // Slow down in case of status code 429(too many requests, rate limit hit)
                            if (response.status == HttpStatus.TOO_MANY_REQUESTS) {
                                sleep(2000)
                            }
                            log.info "Post request to ${url} is failed with status code ${response.status}. Retrying..."
                            retryResponse = executeHttpPostRequestSync(url, headers, requestBody, retries - 1)
                        } else {
                            throw new Exception("Max retries exhausted. Request failed!")
                        }
                    })

            if ([HttpStatus.OK, HttpStatus.CREATED].contains(response.status)) {
                return response.getBody()
            }
            // In case of initial failure return the response from retry
            else {
                return retryResponse
            }
        } catch (UnirestException ex) {
            log.info "Post request to ${url} failed with message ${ex.message}"
            throw new Exception("Post request to ${url} failed with message ${ex.message}")
        }
    }

    HttpResponse<JsonNode> executeHttpPostRequestSync(String url, Map<String, String> headers, T requestBody) {
        log.info("Invoking external Sync REST API call to [POST] - ${url}")

        try {
            HttpResponse<JsonNode> response
            if (ObjectUtils.isEmpty(requestBody)) {
                response = Unirest.post(url)
                        .headers(headers)
                        .asJson()
            } else {
                response = Unirest.post(url)
                        .headers(headers)
                        .body(requestBody)
                        .asJson()
            }
            return response

        } catch (UnirestException ex) {
            log.info "Post request to ${url} failed with message ${ex.message}"
            throw new Exception("Post request to ${url} failed with message ${ex.message}")
        }
    }

    HttpResponse<JsonNode> executeHttpDeleteRequestSync(String url, Map<String, String> headers) {
        log.info("Invoking external Sync REST API call to [DELETE] - ${url}")

        try {
            HttpResponse<JsonNode> response = Unirest.delete(url)
                    .headers(headers)
                    .asJson()
            return response
        } catch (UnirestException ex) {
            log.info "Delete request to ${url} failed with message ${ex.message}"
            throw new Exception("Delete request to ${url} failed with message ${ex.message}")
        }
    }
}
