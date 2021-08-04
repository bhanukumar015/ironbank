package hyperface.cms.commands

import hyperface.cms.domains.Card
import hyperface.cms.util.validation.StringEnumeration

import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero

public class CardLimitsRequest {

    // List of limits to be applied on the card
    @NotBlank(message = "Card limits must not be null/empty")
    List<CardLimit> cardLimits

    // Derived field
    Card card
}

// Card limit to be applied. Request will have a list of CardLimit objects
class CardLimit {
    enum TransactionLimitType {
        ONLINE_TRANSACTION_LIMIT,
        OFFLINE_TRANSACTION_LIMIT,
        CASH_WITHDRAWAL_LIMIT,
        DAILY_LIMIT,
        MONTHLY_LIMIT,
        LIFETIME_LIMIT,
        LIFETIME_COUNT_LIMIT,
        TRANSACTION_DURATION_LIMIT,
        DAILY_CASH_WITHDRAWAL_LIMIT
    }

    // Type of limit
    @StringEnumeration(enumClass = TransactionLimitType.class
            , message = "Invalid limit type. Must be one of [ONLINE_TRANSACTION_LIMIT, OFFLINE_TRANSACTION_LIMIT, CASH_WITHDRAWAL_LIMIT, DAILY_LIMIT, MONTHLY_LIMIT, LIFETIME_LIMIT, LIFETIME_COUNT_LIMIT, TRANSACTION_DURATION_LIMIT, DAILY_CASH_WITHDRAWAL_LIMIT]")
    String type
    // Value for card limit. Date(yyyymmdd) range in case of TRANSACTION_DURATION_LIMIT
    @PositiveOrZero(message = "Limit value must be a non negative integer")
    Double value
    // Limit status : Active or Inactive
    Boolean isEnabled = true
    // Additional margin to be applied when transaction exceeds specified limits
    Double additionalMarginPercentage = 0.0
}


