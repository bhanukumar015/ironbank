package hyperface.cms.service.SwitchProviders.Nium

import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumAddCardCallback
import hyperface.cms.service.SwitchProviders.Nium.CustomerManagement.NiumCreateCustomerCallback
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import kong.unirest.Callback
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import kong.unirest.UnirestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NiumSwitchProvider {

    @Autowired
    NiumCreateCustomerCallback createCustomerCallback

    @Autowired
    NiumAddCardCallback addCardCallback

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumObjectsCreation niumObjectsCreation

    // TODO: remove hardcoding from code. Read from config file?
    private static final String apiKey = 'LnqHbp0r0S4rBQE53FHWW8k1nIHgfRQb4iJk1glR'
    private static final String clientName = 'Hyperface'
    private Logger log = LoggerFactory.getLogger(NiumSwitchProvider.class)

    public static final String niumUrl = 'http://niumproxy.hyperface.in/api/v1/client/c8fbf2b7-1b3e-47a6-9ce2-86539d05d956/'

    public void executeHttpPostRequestAsync(String endpoint, String requestBody, Callback<JsonNode> callback){
        try{
            Unirest.post(niumUrl + endpoint)
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
            HttpResponse<JsonNode> response = Unirest.post(niumUrl + endpoint)
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

    private Map<String,String> getHeaders(){
        Map<String,String> headers = new HashMap<>()
        headers.put('x-api-key', apiKey)
        headers.put('x-client-name', clientName)
        headers.put('Content-Type', 'application/json')
        headers.put('x-request-id', UUID.randomUUID().toString())
        return headers
    }
}
