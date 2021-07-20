package hyperface.cms.commands

import hyperface.cms.domains.Card
import hyperface.cms.model.enums.TransactionType
import hyperface.cms.util.validation.PositiveInteger
import hyperface.cms.util.validation.PositiveValue
import hyperface.cms.util.validation.StringEnumeration

import javax.validation.constraints.NotBlank
import java.time.ZonedDateTime

class CustomerTransactionRequest {
    @NotBlank(message = "cardId must not be null/empty")
    String cardId
    @PositiveValue(message = "transactionAmount must be an double, greater than 0")
    double transactionAmount
    @NotBlank(message = "transactionCurrency must not be null/empty")
    String transactionCurrency
    @NotBlank(message = "transactionDescription must not be null/empty")
    String transactionDescription
    @NotBlank(message = "merchantCategoryCode must not be null/empty")
    String merchantCategoryCode
    @NotBlank(message = "merchantTerminalId must not be null/empty")
    String merchantTerminalId
    ZonedDateTime transactionDate
    @StringEnumeration(enumClass = TransactionType.class,message = "operation must not be null/empty. Must be one of [SETTLEMENT_DEBIT,SETTLEMENT_CREDIT]")
    String transactionType

    //derived field
    Card card
}
