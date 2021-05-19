package hyperface.cms.commands

import hyperface.cms.domains.Card

class SettlementRequest {
    enum SettlementTxnType {
        Debit("Debit"),
        Fee_Debit("Fee_Debit"),
        Settlement_Debit("Settlement_Debit"),
        Settlement_Reversal("Settlement_Reversal"),
        Reversal("Reversal"),
        Fee_Reversal("Fee_Reversal"),
        Settlement_Credit("Settlement_Credit")

        SettlementTxnType(String type) {
            this.type = type
        }
        private String type
    }

    enum SettlementStatus {
        SETTLED("SETTLED"),
        RELEASED("RELEASED")
        SettlementStatus(String status) {
            this.status = status
        }
        private String status
    }
    String cardId
    String retrievalReferenceNumber
    String systemTraceAuditNumber

    SettlementStatus settlementStatus
    SettlementTxnType transactionType

    Double settlementAmount
    String settlementCurrency

    Double billingAmount
    String billingCurrency

    Double transactionAmount
    String transactionCurrency

    Double authAmount
    String authCurrency

    String merchantCategoryCode
    String merchantCategory
    String authorizationCode

    String feeName
    String comments

    // derived parameters
    Card card
}
