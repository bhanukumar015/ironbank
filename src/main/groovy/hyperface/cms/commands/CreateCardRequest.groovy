package hyperface.cms.commands

import hyperface.cms.Constants

class CreateCardRequest {

    String customerId
    String creditAccountId
    String cardProgramId

    // Expiry required only for virtual cards
    String cardExpiry
    // Valid values: Physical, Virtual, VirtualUpgradeToPhysical
    Constants.CardType cardType
}
