package hyperface.cms.service.SwitchProviders.Nium.Utility

import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import org.springframework.stereotype.Component

import java.security.InvalidParameterException

@Component
class NiumObjectsCreation {

    private static ObjectMapper objectMapper = new ObjectMapper()

    public static String createNiumRequestCustomer(Customer customer)
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

    public String createNiumRequestCard(CreateCardRequest cardRequest, CreditCardProgram creditCardProgram){
        Object niumCard = new Object(){
            String cardIssuanceAction = "NEW"
            String cardFeeCurrencyCode = creditCardProgram.baseCurrency
            String cardExpiry = cardRequest.cardExpiry
            String cardType = CardType.fromString(cardRequest.cardType)
            String logoId = creditCardProgram.cardLogoId
            String plasticId = creditCardProgram.cardPlasticId
        }
        return objectMapper.writeValueAsString(niumCard)
    }

    // Enums for Nium card creation
    enum CardType {
        GPR_PHY("Physical"),
        GPR_VIR("Virtual"),
        GPR_VIR_UP2PHY("VirtualUpgradeToPhysical");

        private String text;

        CardType(String text) {
            this.text = text;
        }

        public static CardType fromString(String text) {
            for (CardType b : values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            throw new InvalidParameterException("Value ${text} not supported for CardTypes");
        }
    }
}
