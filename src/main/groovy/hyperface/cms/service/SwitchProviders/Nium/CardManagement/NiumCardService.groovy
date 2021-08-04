package hyperface.cms.service.SwitchProviders.Nium.CardManagement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.service.RestCallerService
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumRestUtils
import io.vavr.control.Either
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
@Slf4j
class NiumCardService {

    @Autowired
    NiumCreateCardCallback createCardCallback

    @Autowired
    NiumObjectsCreation niumObjectsCreation

    @Autowired
    RestCallerService restCallerService

    @Autowired
    NiumRestUtils niumRestUtils

    private static final int MAX_RETRIES = 3
    private static ObjectMapper objectMapper = new ObjectMapper()

    public static final String createCardEndpoint = "customer/%s/wallet/%s/card"
    public static final String cardActionEndpoint = "customer/%s/wallet/%s/card/%s/cardAction"
    public static final String cardSetPinEndpoint = "customer/%s/wallet/%s/card/%s/pin"
    public static final String activateCardEndpoint = "customer/%s/wallet/%s/card/%s/activate"

    Map<String, Object> createCard(CreateCardRequest createCardRequest, CreditCardProgram creditCardProgram,
                                   Map<String, Object> customerSwitchMetadata) {
        String customerHashId = customerSwitchMetadata.get('nium.customerHashId')
        String walletId = customerSwitchMetadata.get('nium.walletId')
        String endpoint = String.format(createCardEndpoint, customerHashId, walletId)
        String requestBody = niumObjectsCreation.createNiumRequestCard(createCardRequest, creditCardProgram)
        String response = restCallerService.executeHttpPostRequestSync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, MAX_RETRIES)
        def metadata = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {})
        Map<String, Object> niumCardMetadata = new HashMap<>()
        niumCardMetadata.put("switchCardId", metadata.get('cardHashId'))
        niumCardMetadata.put("maskedCardNumber", metadata.get('maskedCardNumber'))
        return niumCardMetadata
    }

    HttpStatus createCardAsync(CreateCardRequest createCardRequest, CreditCardProgram creditCardProgram,
                               Map<String, Object> customerSwitchMetadata) {
        String customerHashId = customerSwitchMetadata.get('nium.customerHashId')
        String walletId = customerSwitchMetadata.get('nium.walletId')
        String endpoint = String.format(createCardEndpoint, customerHashId, walletId)
        String requestBody = niumObjectsCreation.createNiumRequestCard(createCardRequest, creditCardProgram)
        createCardCallback.retries = MAX_RETRIES
        createCardCallback.cardRequest = createCardRequest
        createCardCallback.cardProgram = creditCardProgram
        createCardCallback.endpoint = endpoint
        restCallerService.executeHttpPostRequestAsync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, createCardCallback)
        // TODO: send appropriate response
        return HttpStatus.OK
    }

    Either<GenericErrorResponse, String> invokeCardAction(CardBlockActionRequest req){
        String customerHashId = req.card.creditAccount.customer.switchMetadata.get('nium.customerHashId')
        String walletId = req.card.creditAccount.customer.switchMetadata.get('nium.walletId')
        String endpoint = String.format(cardActionEndpoint, customerHashId, walletId, req.card.switchCardId)
        String requestBody = niumObjectsCreation.createCardActionRequest(req)
        try{
            String response = restCallerService.executeHttpPostRequestSync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, MAX_RETRIES)
            def metadata = objectMapper.readValue(response, new TypeReference<Map<String,String>>(){})
            return Either.right(metadata.get(Constants.NiumSuccessResponseKey))
        }
        catch(Exception ex){
            return Either.left(new GenericErrorResponse(reason: ex.message))
        }
    }

    Either<GenericErrorResponse,String> setCardPin(SetCardPinRequest request, Map<String,Object> customerSwitchMetadata){
        String customerHashId = customerSwitchMetadata.get('nium.customerHashId')
        String walletId = customerSwitchMetadata.get('nium.walletId')
        String endpoint = String.format(cardSetPinEndpoint, customerHashId, walletId, request.card.switchCardId)
        String requestBody = objectMapper.writeValueAsString(new Object(){
            String pinBlock = new String(Base64.encodeBase64(request.cardPin.getBytes()))
        })
        String response = restCallerService.executeHttpPostRequestSync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, MAX_RETRIES)
        def metadata = objectMapper.readValue(response, new TypeReference<Map<String,String>>(){})
        return Either.right(metadata.get(Constants.NiumSuccessResponseKey))
    }

    Either<GenericErrorResponse,String> activateCard(Card card){
        Customer customer = card?.creditAccount?.customer
        if(customer == null) {
            return Either.left(new GenericErrorResponse(reason:  "No customer assigned to card with id ${card.id}"))
        }
        String switchCardHashId = card.switchCardId
        String customerHashId = customer.switchMetadata.get('nium.customerHashId')
        String walletId = customer.switchMetadata.get('nium.walletId')
        String endpoint = String.format(activateCardEndpoint, customerHashId, walletId, switchCardHashId)
        try{
            String response = restCallerService.executeHttpPostRequestSync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), null, MAX_RETRIES)
            def metadata = objectMapper.readValue(response, new TypeReference<Map<String,String>>(){})
            if(metadata.get('status') == 'Active'){
                return Either.right("Success")
            }
            return Either.left(new GenericErrorResponse(reason: "Card Activation failed with error:" +
                    " ${metadata.get('errors')}"))
        }
        catch(Exception ex){
            return Either.left(new GenericErrorResponse(reason: "Card activation request for card Id ${card.id} " +
                    "with Nium failed with message: ${ex.message}"))
        }
    }
}
