package hyperface.cms.commands

import hyperface.cms.domains.Card
import hyperface.cms.domains.batch.CurrencyConversion
import hyperface.cms.model.enums.SovereigntyIndicator
import hyperface.cms.model.enums.TransactionType

import java.time.ZonedDateTime

class CustomerTransactionRequest {

    String cardId
    double transactionAmount
    String transactionCurrency
    String transactionDescription
    String merchantCategoryCode
    String merchantTerminalId
    ZonedDateTime transactionDate
    String transactionType
    Card card
}
