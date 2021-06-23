package hyperface.cms.service.SwitchProviders.Nium

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import kong.unirest.Callback
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import kong.unirest.UnirestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class NiumSwitchProvider {

    @Autowired
    NiumCreateCustomerCallback createCustomerCallback

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumObjectsCreation niumObjectsCreation

    // TODO: remove hardcoding from code. Read from config file?
    private static final String apiKey = 'LnqHbp0r0S4rBQE53FHWW8k1nIHgfRQb4iJk1glR'
    private static final String clientName = 'Hyperface'
    private static final int MAX_RETRIES = 3
    private static ObjectMapper objectMapper = new ObjectMapper()
    private Logger log = LoggerFactory.getLogger(NiumSwitchProvider.class)

    public static final String niumUrl = 'http://niumproxy.hyperface.in/api/v1/client/c8fbf2b7-1b3e-47a6-9ce2-86539d05d956/'
    public static final String createCustomerEndpoint = 'customer'
    public static final String createCardEndpoint = "customer/%s/wallet/%s/card"

    public HttpStatus createCustomer(Customer customer){
        String requestBody = NiumObjectsCreation.createNiumRequestCustomer(customer)
        try{
            String response = executeHttpPostRequestSync(createCustomerEndpoint, requestBody, MAX_RETRIES)
            def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
            Map<String, Object> niumCustomerMetadata = new HashMap<>()
            niumCustomerMetadata.put("nium.customerHashId", metadata.get('customerHashId'))
            niumCustomerMetadata.put("nium.walletId", metadata.get('walletHashId'))
            customer.switchMetadata = niumCustomerMetadata
            customerRepository.save(customer)
        }
        catch (Exception ex)
        {
            throw new Exception("Customer creation request with Nium failed with message: ${ex.message}")
        }
        // TODO: send appropriate response
        return HttpStatus.OK
    }

    public HttpStatus createCustomerAsync(Customer customer){
        String requestBody = NiumObjectsCreation.createNiumRequestCustomer(customer)
        createCustomerCallback.customer = customer
        createCustomerCallback.retries = MAX_RETRIES
        executeHttpPostRequestAsync(createCustomerEndpoint, requestBody, createCustomerCallback)
        // TODO: send appropriate response
        return HttpStatus.OK
    }

    public Map<String, Object> createCard(CreateCardRequest createCardRequest, CreditCardProgram creditCardProgram){
        Customer customer = customerRepository.findById(createCardRequest.customerId).get()
        String customerHashId = customer.switchMetadata.get('nium.customerHashId')
        String walletId = customer.switchMetadata.get('nium.walletId')
        String endpoint = String.format(createCardEndpoint, customerHashId, walletId)
        try{
            String requestBody = niumObjectsCreation.createNiumRequestCard(createCardRequest, creditCardProgram)
            String response = executeHttpPostRequestSync(endpoint, requestBody, MAX_RETRIES)
            def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
            Map<String, Object> niumCardMetadata = new HashMap<>()
            niumCardMetadata.put("switchCardId", metadata.get('cardHashId'))
            niumCardMetadata.put("maskedCardNumber", metadata.get('maskedCardNumber'))
            return niumCardMetadata
        }
        catch(Exception ex){
            throw new Exception("Card creation request for customerId ${createCardRequest.customerId} " +
                    "with Nium failed with message: ${ex.message}")
        }
    }

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
            HttpResponse<JsonNode> response =  Unirest.post(niumUrl + endpoint)
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
