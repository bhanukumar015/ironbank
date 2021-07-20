package hyperface.cms.commands

import java.time.ZonedDateTime

class CustomerTransactionResponse {
    String id
    Double transactionAmount
    String transactionCurrency
    Double billingAmount
    String billingCurrency
    String cardId
    ZonedDateTime transactionDate
    String transactionDescription
    String transactionStatus
}
