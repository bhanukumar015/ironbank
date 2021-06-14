package hyperface.cms.service.SwitchProviders.Nium

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import org.apache.http.HttpException
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.nio.client.HttpAsyncClient
import org.apache.http.util.EntityUtils
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

    // TODO: remove hardcoding from code. Read from config file?
    private static final String niumUrl = 'http://niumproxy.hyperface.in/api/v1/client/c8fbf2b7-1b3e-47a6-9ce2-86539d05d956/'
    private static final String apiKey = 'LnqHbp0r0S4rBQE53FHWW8k1nIHgfRQb4iJk1glR'
    private static final String clientName = 'Hyperface'
    private static final int MAX_RETRIES = 3
    private static ObjectMapper objectMapper = new ObjectMapper()

    private Logger log = LoggerFactory.getLogger(NiumSwitchProvider.class)

    public static final String createCustomerEndpoint = 'customer'

    public HttpStatus createCustomer(Customer customer){
        String requestBody = createNiumRequestCustomer(customer)
        try{
            String response = executeSyncHttpPostRequest(createCustomerEndpoint, requestBody, MAX_RETRIES)
            def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
            Map<String, Object> niumMetadata = new HashMap<>()
            niumMetadata.put("nium.customerHashId", metadata.get('customerHashId'))
            niumMetadata.put("nium.walletId", metadata.get('walletHashId'))
            customer.switchMetadata = niumMetadata
            customerRepository.save(customer)
        }
        catch (HttpException ex)
        {
            throw new Exception("Customer creation request with Nium failed with message: ${ex.message}")
        }
        // TODO: send appropriate response
        return HttpStatus.OK
    }

    public void createCustomerAsync(Customer customer){
        String requestBody = createNiumRequestCustomer(customer)
        createCustomerCallback.customer = customer
        createCustomerCallback.retries = MAX_RETRIES
        executeAsyncHttpPostRequest(createCustomerEndpoint, requestBody, createCustomerCallback)
    }

    public void executeAsyncHttpPostRequest(String endpoint, String body, FutureCallback<HttpResponse> callback){
        HttpAsyncClient client = HttpAsyncClients.createDefault()
        client.start()

        HttpPost request = new HttpPost(niumUrl + endpoint)
        request.setHeader('Content-Type', 'application/json')
        request.setHeader('x-api-key', apiKey)
        request.setHeader('x-client-name', clientName)
        String requestId = UUID.randomUUID().toString()
        log.info "Sending POST request to Nium with request Id: ${requestId}"
        request.setHeader('x-request-id', requestId)
        StringEntity entity = new StringEntity(body)
        request.setEntity(entity)

        client.execute(request, callback)
    }

    public String executeSyncHttpPostRequest(String endpoint, String body, int retries){
        CloseableHttpClient client = HttpClients.createDefault()

        HttpPost request = new HttpPost(niumUrl + endpoint)
        request.setHeader('Content-Type', 'application/json')
        request.setHeader('x-api-key', apiKey)
        request.setHeader('x-client-name', clientName)
        String requestId = UUID.randomUUID().toString()
        log.info "Sending POST request to Nium with request Id: ${requestId}"
        request.setHeader('x-request-id', requestId)
        StringEntity entity = new StringEntity(body)
        request.setEntity(entity)

        try {
            HttpResponse response = client.execute(request)
            int statusCode = response.getStatusLine().getStatusCode()

            if (statusCode == 200) {
                return EntityUtils.toString(response.getEntity())
            }
            else if (retries > 0) {
                log.info "POST request failed with status code ${statusCode}. Retrying..."
                if (statusCode == 429) {
                    sleep(30000)
                }
                executeSyncHttpPostRequest(endpoint, body, retries - 1)
            }
            else {
                log.info "Max retries exhausted! Returning failure"
                throw new HttpException("POST request failed with status code ${statusCode}")
            }
        }
        catch (HttpException ex)
        {
            if(retries > 0)
            {
                log.info "POST request failed with message ${ex.message}. Retrying..."
                executeSyncHttpPostRequest(endpoint, body, retries - 1)
            }
            else{
                throw new HttpException("POST request failed with message ${ex.message}")
            }
        }
    }

    private String createNiumRequestCustomer(Customer customer)
    {
        Object niumCustomer = new Object(){
            String firstName = customer.firstName
            String middleName = customer.middleName
            String lastName = customer.lastName
            String preferredName = customer.firstName
            String dateOfBirth = customer.dateOfBirth
            String nationality = customer.nationality
            String email = customer.email
            String countryCode = customer.countryCode
            String mobile = customer.mobile
            String deliveryAddress1 = customer.currentAddress.line1
            String deliveryAddress2 = customer.currentAddress.line2
            String deliveryCity = customer.currentAddress.city
            String deliveryLandmark = customer.currentAddress.landmark
            String deliveryState = customer.currentAddress.state
            String deliveryZipCode = customer.currentAddress.pincode
            String deliveryCountry = customer.currentAddress.countryCodeIso
            String billingAddress1 = customer.currentAddress.line1
            String billingAddress2 = customer.currentAddress.line2
            String billingCity = customer.currentAddress.city
            String billingLandmark = customer.currentAddress.landmark
            String billingZipCode = customer.currentAddress.pincode
            String billingCountry = customer.currentAddress.countryCodeIso
        }
        return objectMapper.writeValueAsString(niumCustomer)
    }
}
