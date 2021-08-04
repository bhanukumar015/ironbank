package hyperface.cms.service.SwitchProviders.Nium.Utility

import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.CreateCustomerRequest
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.repository.CardRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.security.InvalidParameterException

@Component
class NiumObjectsCreation {

    @Autowired
    CardRepository cardRepository

    private static ObjectMapper objectMapper = new ObjectMapper()

    static String createNiumRequestCustomer(CreateCustomerRequest req)
    {
        Object niumCustomer = new Object(){
            String firstName = req.firstname
            String middleName = req.middlename
            String lastName = req.lastname
            String preferredName = req.firstname
            String dateOfBirth = req.dateOfBirth
            String nationality = req.nationality
            String email = req.emailAddress
            String countryCode = req.countryCode
            String mobile = req.mobileNumber
            String deliveryAddress1 = req.currentAddress.line1
            String deliveryAddress2 = req.currentAddress.line2
            String deliveryCity = req.currentAddress.city
            String deliveryLandmark = req.currentAddress.landmark
            String deliveryState = req.currentAddress.state
            String deliveryZipCode = req.currentAddress.pincode
            String deliveryCountry = req.currentAddress.countryCodeIso
            String billingAddress1 = req.currentAddress.line1
            String billingAddress2 = req.currentAddress.line2
            String billingCity = req.currentAddress.city
            String billingLandmark = req.currentAddress.landmark
            String billingZipCode = req.currentAddress.pincode
            String billingCountry = req.currentAddress.countryCodeIso
        }
        return objectMapper.writeValueAsString(niumCustomer)
    }

    String createNiumRequestCard(CreateCardRequest cardRequest, CreditCardProgram creditCardProgram){
        Object niumCard = new Object(){
            String cardIssuanceAction = (cardRequest.isAddOn) ? "ADD_ON" : "NEW"
            String demogOverridden = !cardRequest.isPrimaryCardHolder
            String firstName = cardRequest.addOnCardHolder?.firstName
            String middleName = cardRequest.addOnCardHolder?.middleName
            String lastName = cardRequest.addOnCardHolder?.lastName
            String email = cardRequest.addOnCardHolder?.email
            String mobile = cardRequest.addOnCardHolder?.mobileNumber
            String cardFeeCurrencyCode = creditCardProgram.baseCurrency
            String cardExpiry = cardRequest.cardExpiry
            String cardType = NiumCardType.fromString(cardRequest.cardType)
            String logoId = creditCardProgram.cardLogoId
            String plasticId = creditCardProgram.cardPlasticId
            String cardHashId = (cardRequest.isAddOn)
                    ? cardRepository.findById(cardRequest.primaryCardId)?.get()?.switchCardId
                    : null
        }
        return objectMapper.writeValueAsString(niumCard)
    }

    String createCardActionRequest(CardBlockActionRequest cardBlockActionRequest){
        Object cardAction = new Object(){
            String reason = cardBlockActionRequest.reason
            String blockAction = cardBlockActionRequest.blockAction
        }
        return objectMapper.writeValueAsString(cardAction)
    }

    // Enums for Nium object creation
    enum NiumCardType {
        GPR_PHY("Physical"),
        GPR_VIR("Virtual"),
        GPR_VIR_UP2PHY("VirtualUpgradeToPhysical")

        private String text

        NiumCardType(String text) {
            this.text = text
        }

        public static NiumCardType fromString(String text) {
            for (NiumCardType b : values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b
                }
            }
            throw new InvalidParameterException("Value ${text} not supported for Nium card types")
        }
    }
}
