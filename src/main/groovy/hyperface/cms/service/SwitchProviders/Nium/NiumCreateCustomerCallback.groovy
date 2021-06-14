package hyperface.cms.service.SwitchProviders.Nium

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import org.apache.http.HttpResponse
import org.apache.http.concurrent.FutureCallback
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NiumCreateCustomerCallback implements FutureCallback<HttpResponse> {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumSwitchProvider niumSwitchProvider

    Customer customer
    int retries

    private static ObjectMapper objectMapper = new ObjectMapper()

    private Logger log = LoggerFactory.getLogger(NiumCreateCustomerCallback.class)

    @Override
    void completed(HttpResponse result) {
        int responseCode = result.getStatusLine().getStatusCode()
        if(responseCode != 200 && retries > 0){
            // Response code 429 = Too many requests. Wait before retrying
            // TODO: Update the sleep duration(currently set to 30s) for response code 429.
            //  Need to verify rate limiting from Nium docs
            if(responseCode == 429){
                sleep(30000)
            }
            log.info "Retrying async call to nium. Retries left: ${retries}"
            String requestBody = objectMapper.writeValueAsString(customer)
            this.retries -= 1
            niumSwitchProvider.executeAsyncHttpPostRequest(NiumSwitchProvider.createCustomerEndpoint, requestBody, this)
        }

        else if(responseCode == 200){
            String response = EntityUtils.toString(result.getEntity())
            def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
            Map<String, Object> niumMetadata = new HashMap<>()
            niumMetadata.put("nium.customerHashId", metadata.get('customerHashId'))
            niumMetadata.put("nium.walletId", metadata.get('walletHashId'))
            customer.switchMetadata = niumMetadata
            customerRepository.save(customer)
        }
    }

    @Override
    void failed(Exception ex) {
        if (retries > 0){
            log.info "Async call to Nium failed with exception: ${ex.message}"
            log.info "Retrying async call to nium. Retries left: ${retries}"
            String requestBody = objectMapper.writeValueAsString(customer)
            this.retries -= 1
            niumSwitchProvider.executeAsyncHttpPostRequest(NiumSwitchProvider.createCustomerEndpoint, requestBody, this)
        }
    }

    @Override
    void cancelled() {
        // TODO: check if this needs to be implemented
    }
}
