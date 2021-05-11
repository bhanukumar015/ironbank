package hyperface.cms.commands

import hyperface.cms.Constants
import hyperface.cms.domains.Card
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTxn

class AuthorizationRequest {

    enum TransactionType {
        DEBIT("DEBIT"),
        REVERSAL("REVERSAL"),
        PARTIAL_REVERSAL("PARTIAL_REVERSAL")

        TransactionType(String value) {
            this.value = value
        }
        private String value
    }

    Long cardId

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
    TransactionType transactionType
    String posEntryMode
    String posConditionCode
    String posEntryCapabilityCode
    String retrievalReferenceNumber
    String systemTraceAuditNumber
    String acquiringInstitutionCountryCode
    String acquiringInstitutionCode

    // derived parameters
    Card card

    public CustomerTxn.TxnType getTxnType() {
        switch(this.transactionType) {
            case TransactionType.DEBIT:
                return CustomerTxn.TxnType.Authorize
            case TransactionType.REVERSAL:
                return CustomerTxn.TxnType.Refund
            case TransactionType.PARTIAL_REVERSAL:
                return CustomerTxn.TxnType.Refund
        }
        return null
    }
}
