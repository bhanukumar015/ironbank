package hyperface.cms.domains

import hyperface.cms.domains.converters.SimpleJsonConverter
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Card implements PaymentInstrument {

    @Id
    @GenericGenerator(name = "card_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "card_id")
    String id

    @ManyToOne
    @JoinColumn(name="card_bin_id")
    CardBin cardBin

    String switchCardId

    String lastFourDigits

    int cardExpiryMonth
    int cardExpiryYear

    Boolean physicallyIssued
    Boolean virtuallyIssued

    Boolean virtualCardActivatedByCustomer = false
    Boolean physicalCardActivatedByCustomer = false

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

    Boolean isLocked = false
    Boolean hotlisted = false

    @ManyToOne
    @JoinColumn(name="credit_account_id")
    CreditAccount creditAccount

    @ManyToOne
    CreditCardProgram cardProgram

    @ManyToOne
    Client client

    @ManyToOne
    Bank bank
}

public class TransactionLimit{
    Double value
    Double additionalMarginPercentage
    Boolean isEnabled
}
