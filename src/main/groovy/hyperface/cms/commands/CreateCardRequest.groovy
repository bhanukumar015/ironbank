package hyperface.cms.commands

import hyperface.cms.Constants.CardType
import hyperface.cms.util.validation.StringEnumeration

import javax.validation.constraints.NotBlank

class CreateCardRequest {

    @NotBlank(message = "Customer id must not be blank/null")
    String customerId
    @NotBlank(message = "Credit account id must not be blank/null")
    String creditAccountId
    @NotBlank(message = "Credit card program id must not be blank/null")
    String cardProgramId

    Boolean isAddOn = false
    Boolean isPrimaryCardHolder = true
    CardHolder addOnCardHolder
    String primaryCardId

    // Expiry required only for virtual cards
    String cardExpiry
    @StringEnumeration(enumClass = CardType.class, message = "Card type must not be null/empty. Must be one of [Physical, Virtual, VirtualUpgradeToPhysical, Phygital].")
    String cardType
}

class CardHolder{
    String firstName
    String middleName
    String lastName
    String email
    String mobileNumber
}
