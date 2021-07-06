package hyperface.cms.commands

// Request to block/unblock a card
class CardBlockActionRequest {

    // Enum to specify the block action for the card
    enum BlockAction{ TEMPORARYBLOCK, PERMANENTBLOCK, UNBLOCK }

    // Enum to specify the reason for card action
    enum BlockActionReason{ FRAUD, CARDLOST, CARDSTOLEN, DAMAGED }

    String cardId

    BlockAction blockAction

    // Reason for the action
    BlockActionReason reason
}




