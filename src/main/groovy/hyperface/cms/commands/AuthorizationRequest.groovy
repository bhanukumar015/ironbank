package hyperface.cms.commands

import hyperface.cms.domains.Card

class AuthorizationRequest {

    Long cardId

    double billingAmount
    String billingCurrency // currency of the customer
    double transactionAmount
    String transactionCurrency

    String processingCode
    String description
    String merchantCategoryCode
    String merchantTerminalId
    String merchantNameLocation
    String transactionId
    String transactionType
    String posEntryMode
    String posConditionCode
    String posEntryCapabilityCode
    String retrievalReferenceNumber
    String systemTraceAuditNumber
    String acquiringInstitutionCountryCode
    String acquiringInstitutionCode

    // derived parameters
    Card card
}
