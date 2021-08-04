package hyperface.cms.service.SwitchProviders.Nium.CustomerManagement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.RestCallerService
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumRestUtils
import kong.unirest.Callback
import kong.unirest.HttpResponse
import kong.unirest.HttpStatus
import kong.unirest.JsonNode
import kong.unirest.UnirestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NiumCreateCustomerCallback implements Callback<JsonNode> {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumRestUtils niumRestUtils

    @Autowired
    RestCallerService restCallerService

    Customer customer
    String endpoint
    int retries

    private Logger log = LoggerFactory.getLogger(NiumCreateCustomerCallback.class)
    private static ObjectMapper objectMapper = new ObjectMapper()

    @Override
    void completed(HttpResponse<JsonNode> response) {
        if (response.status == HttpStatus.OK) {
            log.info "POST request with request id ${response.getHeaders().get('x-request-id')} " +
                    "to Nium passed"
            def metadata = objectMapper.readValue(response.getBody().toString(), new TypeReference<Map<String, Object>>() {
            })
            Map<String, Object> niumMetadata = new HashMap<>()
            niumMetadata.put("nium.customerHashId", metadata.get('customerHashId'))
            niumMetadata.put("nium.walletId", metadata.get('walletHashId'))
            customer.switchMetadata = niumMetadata
            customerRepository.save(customer)
        } else if (retries > 0) {
            // Slow down in case of status code 429(too many requests, rate limit hit)
            if (response.status == HttpStatus.TOO_MANY_REQUESTS) {
                sleep(2000)
            }
            log.info "Request to Nium failed with status code ${response.status}. Retrying..."
            String requestBody = NiumObjectsCreation.createNiumRequestCustomer(customer)
            this.retries -= 1
            restCallerService.executeHttpPostRequestAsync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, this)
        } else {
            log.info "Request to Nium failed with status code ${response.status}"
            throw new Exception("Retries exhausted. Request failed!")
        }
    }

    @Override
    void failed(UnirestException e) {
        if (retries > 0) {
            log.info "Request to Nium failed with message ${e.message}. Retrying..."
            String requestBody = NiumObjectsCreation.createNiumRequestCustomer(customer)
            this.retries -= 1
            restCallerService.executeHttpPostRequestAsync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, this)
        } else {
            throw new Exception("Retries exhausted. Request to create customer failed with message ${e.message}")
        }
    }

    @Override
    void cancelled() {
    }
}
