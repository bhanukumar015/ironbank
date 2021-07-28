package hyperface.cms.domains

import hyperface.cms.domains.converters.SimpleJsonConverter
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class CardControl {
    @Id
    @GenericGenerator(name = "card_control_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "card_control_id")
    String id

    Boolean cardSuspendedByCustomer = false
    Boolean enableOverseasTransactions = false
    Boolean enableOfflineTransactions = false
    Boolean enableNFC = false
    Boolean enableOnlineTransactions = false
    Boolean enableCashWithdrawal = false
    Boolean enableMagStripe = false

    @Convert(converter = SimpleJsonConverter.class)
    TransactionLimit dailyCashWithdrawalLimit
    @Convert(converter = SimpleJsonConverter.class)
    TransactionLimit dailyTransactionLimit
    @Convert(converter = SimpleJsonConverter.class)
    TransactionLimit perTransactionLimit
    @Convert(converter = SimpleJsonConverter.class)
    TransactionLimit monthlyTransactionLimit
    @Convert(converter = SimpleJsonConverter.class)
    TransactionLimit lifetimeTransactionLimit
}

public class TransactionLimit{
    Double value
    Double additionalMarginPercentage
    Boolean isEnabled
}

