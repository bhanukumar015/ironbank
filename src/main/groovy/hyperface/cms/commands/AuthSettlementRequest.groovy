package hyperface.cms.commands

import hyperface.cms.domains.Card
import hyperface.cms.model.enums.TransactionType
import hyperface.cms.util.validation.PositiveValue
import hyperface.cms.util.validation.StringEnumeration

import javax.validation.constraints.NotBlank
import java.time.ZonedDateTime

class AuthSettlementRequest {
    @NotBlank(message = "cardId must not be null/empty")
    String cardId
    @NotBlank(message = "transactionId must not be null/empty")
    String transactionId
    @PositiveValue(message = "transactionAmount must be an double, greater than 0")
    Double settlementAmount
    @NotBlank(message = "transactionCurrency must not be null/empty")
    String settlementCurrency
    ZonedDateTime transactionDate
    @StringEnumeration(enumClass = TransactionType.class,message = "operation must not be null/empty. Must be one of [SETTLEMENT_DEBIT,SETTLEMENT_CREDIT]")
    String transactionType

    Card card
}
