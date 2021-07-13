package hyperface.cms.domains

import hyperface.cms.Constants
import hyperface.cms.domains.rewards.Offer
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import java.time.ZonedDateTime
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
    Constants.Currency baseCurrency

    // program defaults
    Integer defaultDailyTransactionLimit
    Integer defaultDailyCashWithdrawalLimit

    Integer annualizedPercentageRateInBps

    String cardLogoId
    String cardPlasticId

    String startingCardNumber
    String endingCardNumber
    String lastUsedCardNumber

    Boolean domesticUsage
    Boolean internationalUsage
    Boolean cardIssuing = true

    ZonedDateTime dueDate
    Integer gracePeriodInDays
    Double minimumAmountDueFloor

    Double overLimitAuthPct
    Double cashAdvanceLimitPct

    @ManyToOne
    CreditCardScheduleOfCharges scheduleOfCharges

    @ManyToOne
    @JoinColumn(name="card_bin_id")
    CardBin cardBin

    @ManyToOne
    Bank bank

    @ManyToOne
    Client client

    @OneToMany(mappedBy = "creditCardProgram")
    List<Offer> offers

}
