package hyperface.cms.domains

import hyperface.cms.Constants
import hyperface.cms.domains.rewards.Offer
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class CreditCardProgram extends HyperfaceProgram {
    @Id
    @GenericGenerator(name = "card_program_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "card_program_id")
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

    Integer annualizedPercentageRateInBps

    String cardLogoId
    String cardPlasticId

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
    CardBin cardBin

    @ManyToOne
    Bank bank

    @ManyToOne
    Client client

    enum DisableLevel{
        DAILY,
        WEEKLY,
        MONTHLY,
        LIFETIME,
        MANUAL
    }
    @OneToMany(mappedBy = "creditCardProgram")
    List<Offer> offers
}
