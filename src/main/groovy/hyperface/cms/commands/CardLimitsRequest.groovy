package hyperface.cms.commands

public class CardLimitsRequest {
    // Card limit to be applied. Request will have a list of CardLimit objects
    class CardLimit {
        enum TransactionLimitType {
            PER_TRANSACTION_LIMIT,
            DAILY_LIMIT,
            MONTHLY_LIMIT,
            LIFETIME_LIMIT,
            LIFETIME_COUNT_LIMIT,
            TRANSACTION_DURATION_LIMIT,
            DAILY_CASH_WITHDRAWAL_LIMIT
        }

        // Type of limit
        TransactionLimitType type
        // Value for card limit. Date(yyyymmdd) range in case of TRANSACTION_DURATION_LIMIT
        Double value
        // Limit status : Active or Inactive
        Boolean isEnabled
        // Additional margin to be applied when transaction exceeds specified limits
        Double additionalMarginPercentage
    }

    String cardId
    // List of limits to be applied on the card
    List<CardLimit> cardLimits
}


