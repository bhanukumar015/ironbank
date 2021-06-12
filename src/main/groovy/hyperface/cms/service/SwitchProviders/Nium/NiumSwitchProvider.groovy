package hyperface.cms.service.SwitchProviders.Nium

import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.domains.Customer
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.nio.client.HttpAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service


@Service
class NiumSwitchProvider {

    NiumSwitchProvider(){
        MAX_RETRIES = 3
    }

    @Autowired
    NiumCreateCustomerCallback createCustomerCallback

    // TODO: remove hardcoding from code. Read from config file?
    private static final String niumUrl = 'http://niumproxy.hyperface.in/api/v1/client/c8fbf2b7-1b3e-47a6-9ce2-86539d05d956/'
    private static final String apiKey = 'LnqHbp0r0S4rBQE53FHWW8k1nIHgfRQb4iJk1glR'
    private static final String clientName = 'Hyperface'
    private final int MAX_RETRIES
    private static ObjectMapper objectMapper = new ObjectMapper()

    public static final String createCustomerEndpoint = 'customer'

    public HttpStatus createCustomer(Customer customer){
        String requestBody = createNiumRequestCustomer(customer)
        createCustomerCallback.customer = customer
        createCustomerCallback.retries = MAX_RETRIES
        executeAsyncHttpPostRequest(createCustomerEndpoint, requestBody, createCustomerCallback)
        // TODO: send appropriate response
        return HttpStatus.OK
    }

    public static void executeAsyncHttpPostRequest(String endpoint, String body, FutureCallback<HttpResponse> callback){
        HttpAsyncClient client = HttpAsyncClients.createDefault()
        client.start()

        HttpPost request = new HttpPost(niumUrl+endpoint)
        request.setHeader('Content-Type', 'application/json')
        request.setHeader('x-api-key', apiKey)
        request.setHeader('x-client-name', clientName)
        request.setHeader('x-request-id', UUID.randomUUID().toString())
        StringEntity entity = new StringEntity(body)
        request.setEntity(entity)

        client.execute(request, callback)
    }

    private static String createNiumRequestCustomer(Customer customer)
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
