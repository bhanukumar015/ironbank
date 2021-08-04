package hyperface.cms.domains

import hyperface.cms.Constants
import hyperface.cms.domains.kyc.KycOption
import hyperface.cms.domains.rewards.Offer
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import java.time.ZonedDateTime

@Entity
class CreditCardProgram extends HyperfaceProgram {

    enum DisableLevel {
        DAILY,
        WEEKLY,
        MONTHLY,
        LIFETIME,
        MANUAL
    }

    enum CardActivation {AUTO, FIRST_TRANSACTION, MANUAL}

    @Id
    @GenericGenerator(name = "credit_card_program_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "credit_card_program_id")
    String id

    String name

    Boolean isActive = false
    @Enumerated(value = EnumType.STRING)
    DisableLevel disableLevel

    @Enumerated(value = EnumType.STRING)
    Constants.Currency baseCurrency

    // program defaults
    Integer defaultDailyTransactionLimit
    Integer defaultDailyCashWithdrawalLimit
    Integer defaultOnlineTransactionLimit
    Integer defaultOfflineTransactionLimit
    Integer defaultCashWithdrawalLimit
    Integer defaultMonthlyTransactionLimit
    Integer defaultLifetimeTransactionLimit

    Integer annualizedPercentageRateInBps

    String cardLogoId
    String cardPlasticId

    Boolean magStripePresent
    Boolean nfcTagPresent

    String startingCardNumber
    String endingCardNumber
    String lastUsedCardNumber

    @Enumerated(value = EnumType.STRING)
    CardActivation virtualCardActivation
    @Enumerated(value = EnumType.STRING)
    CardActivation physicalCardActivation

    Boolean domesticUsage
    Boolean internationalUsage
    Boolean cardIssuing = true

    ZonedDateTime dueDate
    Integer gracePeriodInDays
    Double minimumAmountDueFloor

    Double overLimitAuthPct
    Double cashAdvanceLimitPct

    // Count for accounts created in the current day
    Integer currentDayAccountCount
    // Max limit for account creation in a day
    Integer dailyAccountLimit
    // Count for accounts created in the current week
    Integer currentWeekAccountCount
    // Max limit for account creation in a week
    Integer weeklyAccountLimit
    // Count for accounts created in the current month
    Integer currentMonthAccountCount
    // Max limit for account creation in a month
    Integer monthlyAccountLimit
    // Count for total accounts created till now
    Integer lifetimeAccountCount
    // Max limit for total account creation
    Integer lifetimeAccountLimit


    @ManyToOne
    CreditCardScheduleOfCharges scheduleOfCharges

    @ManyToOne
    @JoinColumn(name = "card_bin_id")
    CardBin cardBin

    @ManyToOne
    Bank bank

    @ManyToOne
    Client client

    @OneToMany(mappedBy = "creditCardProgram")
    List<Offer> offers

    @OneToMany(mappedBy = "creditCardProgram")
    List<KycOption> kycOptions

}
