package hyperface.cms.commands

import hyperface.cms.domains.Card

class SettlementDebitRequest {
    Long cardId
    String retrievalReferenceNumber
    String systemTraceAuditNumber

    String settlementStatus
    String transactionType // Debit, Fee_Debit, Settlement_Debit

    Double settlementAmount
    String settlementCurrency

    String merchantCategoryCode
    String merchantCategory
    String authorizationCode

    String feeName
    String comments

    // derived parameters
    Card card
}
