package hyperface.cms.service.SwitchProviders.Nium.CardManagement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.NiumSwitchProvider
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class NiumCardService {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumSwitchProvider niumSwitchProvider

    @Autowired
    NiumCreateCardCallback createCardCallback

    @Autowired
    NiumObjectsCreation niumObjectsCreation

    private static final int MAX_RETRIES = 3
    private static ObjectMapper objectMapper = new ObjectMapper()

    public static final String createCardEndpoint = "customer/%s/wallet/%s/card"

    public Map<String, Object> createCard(CreateCardRequest createCardRequest, CreditCardProgram creditCardProgram){
        Customer customer = customerRepository.findById(createCardRequest.customerId)
                .orElseThrow(() -> new IllegalArgumentException("No customer found with customer" +
                        " id ${createCardRequest.customerId}"))
        String customerHashId = customer.switchMetadata.get('nium.customerHashId')
        String walletId = customer.switchMetadata.get('nium.walletId')
        String endpoint = String.format(createCardEndpoint, customerHashId, walletId)
        String requestBody = niumObjectsCreation.createNiumRequestCard(createCardRequest, creditCardProgram)
        String response = niumSwitchProvider.executeHttpPostRequestSync(endpoint, requestBody, MAX_RETRIES)
        def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
        Map<String, Object> niumCardMetadata = new HashMap<>()
        niumCardMetadata.put("switchCardId", metadata.get('cardHashId'))
        niumCardMetadata.put("maskedCardNumber", metadata.get('maskedCardNumber'))
        return niumCardMetadata
    }

    public HttpStatus createCardAsync(CreateCardRequest createCardRequest, CreditCardProgram creditCardProgram){
        Customer customer = customerRepository.findById(createCardRequest.customerId)
                .orElseThrow(() -> new IllegalArgumentException("No customer found with customer" +
                        " id ${createCardRequest.customerId}"))
        String customerHashId = customer.switchMetadata.get('nium.customerHashId')
        String walletId = customer.switchMetadata.get('nium.walletId')
        String endpoint = String.format(createCardEndpoint, customerHashId, walletId)
        String requestBody = niumObjectsCreation.createNiumRequestCard(createCardRequest, creditCardProgram)
        createCardCallback.retries = MAX_RETRIES
        createCardCallback.cardRequest = createCardRequest
        createCardCallback.cardProgram = creditCardProgram
        createCardCallback.endpoint = endpoint
        niumSwitchProvider.executeHttpPostRequestAsync(endpoint, requestBody, createCardCallback)
        // TODO: send appropriate response
        return HttpStatus.OK
    }
}
