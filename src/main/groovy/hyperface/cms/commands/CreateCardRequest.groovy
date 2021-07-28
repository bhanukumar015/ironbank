package hyperface.cms.commands

import hyperface.cms.Constants

class CreateCardRequest {

    String customerId
    String creditAccountId
    String cardProgramId

    Boolean isAddOn
    Boolean isPrimaryCardHolder
    CardHolder addOnCardHolder
    String primaryCardId

    // Expiry required only for virtual cards
    String cardExpiry
    // Valid values: Physical, Virtual, VirtualUpgradeToPhysical
    Constants.CardType cardType
}

class CardHolder{
    String firstName
    String middleName
    String lastName
    String email
    String mobileNumber
}
