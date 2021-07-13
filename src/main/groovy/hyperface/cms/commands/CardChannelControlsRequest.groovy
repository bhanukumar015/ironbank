package hyperface.cms.commands

class CardChannelControlsRequest {
    String cardId
    boolean enableOverseasTransactions
    boolean enableOfflineTransactions
    boolean enableOnlineTransactions
    boolean enableMagStripe
    boolean enableNFC
    boolean enableCashWithdrawl
}
