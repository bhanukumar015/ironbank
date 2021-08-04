package hyperface.cms.domains

import hyperface.cms.domains.converters.TransactionLimitConverter
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

    @Convert(converter = TransactionLimitConverter.class)
    TransactionLimit dailyCashWithdrawalLimit
    @Convert(converter = TransactionLimitConverter.class)
    TransactionLimit dailyTransactionLimit
    @Convert(converter = TransactionLimitConverter.class)
    TransactionLimit onlineTransactionLimit
    @Convert(converter = TransactionLimitConverter.class)
    TransactionLimit offlineTransactionLimit
    @Convert(converter = TransactionLimitConverter.class)
    TransactionLimit cashWithdrawalLimit
    @Convert(converter = TransactionLimitConverter.class)
    TransactionLimit monthlyTransactionLimit
    @Convert(converter = TransactionLimitConverter.class)
    TransactionLimit lifetimeTransactionLimit
}

public class TransactionLimit{
    Double limit
    Double currentValue
    Double additionalMarginPercentage
    Boolean isEnabled
}

