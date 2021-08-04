package hyperface.cms.commands

class AccountTransactionsResponse {
    Boolean hasMore
    Integer count
    Integer offset
    Integer totalCount
    List<TransactionResponse> transactions
}

class TransactionResponse{
    String id
    Double amount
    String description
    Double transactionAmount
    String transactionCurrency
    String txnType
    String postedToLedger
    String mcc
    String mid
    String tid
    String identifiedMerchantLogo
}