package hyperface.cms.commands

import hyperface.cms.domains.Card
import hyperface.cms.util.validation.StringEnumeration

// Request to block/unblock a card
class CardBlockActionRequest {

    // Enum to specify the block action for the card
    enum BlockAction{ TEMPORARYBLOCK, PERMANENTBLOCK, UNBLOCK }

    // Enum to specify the reason for card action
    enum BlockActionReason{ FRAUD, CARDLOST, CARDSTOLEN, DAMAGED }

    @StringEnumeration(enumClass = BlockAction.class, message = "Invalid input for block action. Must be one of [TEMPORARYBLOCK, PERMANENTBLOCK, UNBLOCK]")
    String blockAction

    @StringEnumeration(enumClass = BlockActionReason.class, message = "Invalid input for reason. Must be one of [FRAUD, CARDLOST, CARDSTOLEN, DAMAGED]")
    String reason

    // Derived class
    Card card
}




