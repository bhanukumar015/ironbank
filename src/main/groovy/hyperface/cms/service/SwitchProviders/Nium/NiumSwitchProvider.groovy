package hyperface.cms.service.SwitchProviders.Nium

import groovy.util.logging.Slf4j
import hyperface.cms.config.SwitchProvidersConfig
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.CustomerManagement.NiumCreateCustomerCallback
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import kong.unirest.Callback
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import kong.unirest.UnirestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
@Slf4j
class NiumSwitchProvider {

    @Autowired
    NiumCreateCustomerCallback createCustomerCallback

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumObjectsCreation niumObjectsCreation

    @Autowired
    SwitchProvidersConfig switchProvidersConfig

    public void executeHttpPostRequestAsync(String endpoint, String requestBody, Callback<JsonNode> callback){
        try{
            Unirest.post(switchProvidersConfig.getNiumUrl() + endpoint)
                    .headers(getHeaders())
                    .body(requestBody)
                    .asJsonAsync(callback)
        }
        catch(UnirestException ex){
            log.info "Post request to Nium failed with message ${ex.message}"
            throw new Exception("Post request to Nium failed with message ${ex.message}")
        }
    }

    public String executeHttpPostRequestSync(String endpoint, String requestBody, int retries){
        try{
            String retryResponse = null
            HttpResponse<JsonNode> response = Unirest.post(switchProvidersConfig.getNiumUrl() + endpoint)
                    .headers(getHeaders())
                    .body(requestBody)
                    .asJson()
                    .ifSuccess(response -> {
                        log.info "POST request with request id ${response.getHeaders().get('x-request-id')} " +
                                "to Nium passed"
                    })
                    .ifFailure(response -> {
                        if(retries > 0){
                            // Slow down in case of status code 429(too many requests, rate limit hit)
                            if(response.status == 429){sleep(2000)}
                            log.info "Nium request for create customer failed with status code ${response.status}. Retrying..."
                            retryResponse = executeHttpPostRequestSync(endpoint, requestBody, retries - 1)
                        }
                        else{
                            throw new Exception("Max retries exhausted. Request failed!")
                        }
                    })

            if(response.status == 200){
                return response.getBody()
            }
            // In case of initial failure return the response from retry
            else{
                return retryResponse
            }
        }
        catch(UnirestException ex){
            log.info "Post request to Nium failed with message ${ex.message}"
            throw new Exception("Post request to Nium failed with message ${ex.message}")
        }
    }

    Map<String,String> getHeaders(){
        Map<String,String> headers = new HashMap<>()
        headers.put('x-api-key', switchProvidersConfig.niumAPIKey)
        headers.put('x-client-name', switchProvidersConfig.niumClientName)
        headers.put(HttpHeaders.CONTENT_TYPE, 'application/json')
        headers.put('x-request-id', UUID.randomUUID().toString())
        return headers
    }
}
