package hyperface.cms.service.SwitchProviders

import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.domains.Customer
import hyperface.cms.domains.SwitchProviders.NiumSwitchMetadata
import hyperface.cms.domains.converters.SimpleJsonConverter
import hyperface.cms.repository.CustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
public class NiumSwitchProvider {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    SimpleJsonConverter jsonConverter

    private String endpoint = 'http://niumproxy.hyperface.in/api/v1/client/c8fbf2b7-1b3e-47a6-9ce2-86539d05d956/customer'
    private String apiKey = 'LnqHbp0r0S4rBQE53FHWW8k1nIHgfRQb4iJk1glR'
    private String clientName = 'Hyperface'

    private static ObjectMapper objectMapper = new ObjectMapper();

    public Customer createCustomer(Customer customer){
        def request = (HttpURLConnection) (new URL(endpoint).openConnection())
        def body = objectMapper.writeValueAsString(customer)
        request.setRequestMethod("POST")
        request.setRequestProperty('Content-Type', 'application/json')
        request.setRequestProperty('x-api-key', apiKey)
        request.setRequestProperty('x-client-name', clientName)
        request.setRequestProperty('x-request-id', UUID.randomUUID().toString())
        request.setDoOutput(true)
        request.getOutputStream().write(body.getBytes('UTF-8'))
        try{
            def response = objectMapper.readValue(request.getInputStream().getText(), NiumSwitchMetadata.class)
            Map<String, Object> niumMetadata = new HashMap<>()
            niumMetadata.put("nium.customerHashId", response.customerHashId)
            niumMetadata.put("nium.walletId", response.walletHashId)
            customer.switchMetadata = jsonConverter.convertToDatabaseColumn(niumMetadata)
            customerRepository.save(customer)
        }
        catch(Exception e){
            throw new Exception("createCustomer request to Nium failed with message ${e.message}")
        }
        return customer
    }
}
