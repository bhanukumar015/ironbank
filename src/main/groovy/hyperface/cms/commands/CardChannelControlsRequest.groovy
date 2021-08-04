package hyperface.cms.commands

import hyperface.cms.domains.Card

class CardChannelControlsRequest {
    boolean enableOverseasTransactions
    boolean enableOfflineTransactions
    boolean enableOnlineTransactions
    boolean enableMagStripe
    boolean enableNFC
    boolean enableCashWithdrawal

    // Derived Object
    Card card
}
