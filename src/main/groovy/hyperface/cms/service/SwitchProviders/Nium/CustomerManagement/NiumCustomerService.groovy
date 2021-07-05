package hyperface.cms.service.SwitchProviders.Nium.CustomerManagement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.NiumSwitchProvider
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class NiumCustomerService {
    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumCreateCustomerCallback createCustomerCallback

    @Autowired
    NiumSwitchProvider niumSwitchProvider

    private static final int MAX_RETRIES = 3
    private static ObjectMapper objectMapper = new ObjectMapper()

    public static final String createCustomerEndpoint = 'customer'

    public HttpStatus createCustomer(Customer customer){
        String requestBody = NiumObjectsCreation.createNiumRequestCustomer(customer)
        String response = niumSwitchProvider.executeHttpPostRequestSync(createCustomerEndpoint, requestBody, MAX_RETRIES)
        def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
        Map<String, Object> niumCustomerMetadata = new HashMap<>()
        niumCustomerMetadata.put("nium.customerHashId", metadata.get('customerHashId'))
        niumCustomerMetadata.put("nium.walletId", metadata.get('walletHashId'))
        customer.switchMetadata = niumCustomerMetadata
        customerRepository.save(customer)
        // TODO: send appropriate response
        return HttpStatus.OK
    }

    public HttpStatus createCustomerAsync(Customer customer){
        String requestBody = NiumObjectsCreation.createNiumRequestCustomer(customer)
        createCustomerCallback.customer = customer
        createCustomerCallback.retries = MAX_RETRIES
        createCustomerCallback.endpoint = createCustomerEndpoint
        niumSwitchProvider.executeHttpPostRequestAsync(createCustomerEndpoint, requestBody, createCustomerCallback)
        // TODO: send appropriate response
        return HttpStatus.OK
    }
}
