package hyperface.cms.commands

import hyperface.cms.Constants
import hyperface.cms.domains.Card
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTxn

class AuthorizationRequest {
    String internalReferenceId = UUID.randomUUID().toString()
    String cardId
    Constants.CardSwitch cardSwitch = Constants.CardSwitch.Nium

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
    Constants.TxnType transactionType
    String posEntryMode
    String posConditionCode
    String posEntryCapabilityCode
    String retrievalReferenceNumber
    String systemTraceAuditNumber
    String acquiringInstitutionCountryCode
    String acquiringInstitutionCode

    Date transactionDate

    // derived parameters
    Card card
}
