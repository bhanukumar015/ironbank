package hyperface.cms.service.SwitchProviders.Nium.CardManagement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.Constants
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.NiumSwitchProvider
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import org.apache.commons.codec.binary.Base64
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
    public static final String cardActionEndpoint = "customer/%s/wallet/%s/card/%s/cardAction"
    public static final String cardSetPinEndpoint = "customer/%s/wallet/%s/card/%s/pin"
    public static final String activateCardEndpoint = "customer/%s/wallet/%s/card/%s/activate"

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

    public boolean invokeCardAction(CardBlockActionRequest cardBlockActionRequest
                            , Map<String,Object> customerSwitchMetadata, String switchCardId){
        String customerHashId = customerSwitchMetadata.get('nium.customerHashId')
        String walletId = customerSwitchMetadata.get('nium.walletId')
        String endpoint = String.format(cardActionEndpoint, customerHashId, walletId, switchCardId)
        String requestBody = niumObjectsCreation.createCardActionRequest(cardBlockActionRequest)
        String response = niumSwitchProvider.executeHttpPostRequestSync(endpoint, requestBody, MAX_RETRIES)
        def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
        return (metadata.get(Constants.NiumSuccessResponseKey) == Constants.NiumSuccessResponseValue)
    }

    public boolean setCardPin(SetCardPinRequest setCardPinRequest, Map<String,Object> customerSwitchMetadata
                              , String switchCardId){
        String customerHashId = customerSwitchMetadata.get('nium.customerHashId')
        String walletId = customerSwitchMetadata.get('nium.walletId')
        String endpoint = String.format(cardSetPinEndpoint, customerHashId, walletId, switchCardId)
        String requestBody = objectMapper.writeValueAsString(new Object(){
            String pinBlock = new String(Base64.encodeBase64(setCardPinRequest.cardPin.getBytes()))
        })
        String response = niumSwitchProvider.executeHttpPostRequestSync(endpoint
                            , requestBody, MAX_RETRIES)
        def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
        return (metadata.get(Constants.NiumSuccessResponseKey) == Constants.NiumSuccessResponseValue)
    }

    public boolean activateCard(Card card){
        Customer customer = card?.creditAccount?.customer
        if(customer == null) {
            throw new IllegalArgumentException("No customer assigned to card with id ${card.id}")
        }
        String switchCardHashId = card.switchCardId
        String customerHashId = customer.switchMetadata.get('nium.customerHashId')
        String walletId = customer.switchMetadata.get('nium.walletId')
        String endpoint = String.format(activateCardEndpoint, customerHashId, walletId, switchCardHashId)
        try{
            String response = niumSwitchProvider.executeHttpPostRequestSync(endpoint, null, MAX_RETRIES)
            def metadata = objectMapper.readValue(response, new TypeReference<Map<String,Object>>(){})
            if(metadata.get('status') == 'Active'){
                return true
            }
            throw new Exception("Card Activation failed with error: ${metadata.get('errors')}")
        }
        catch(Exception ex){
            throw new Exception("Card activation request for card Id ${card.id} " +
                    "with Nium failed with message: ${ex.message}")
        }
    }

}
