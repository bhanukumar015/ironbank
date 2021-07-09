package hyperface.cms.domains

import hyperface.cms.Constants
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import java.time.ZonedDateTime

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
    Integer gracePeriod //Days
    Double minimumAmountDue

    Double overlimitAuth //percentage
    Double cashAdvanceLimit //percentage

    @ManyToOne
    CreditCardScheduleOfCharges scheduleOfCharges

    @ManyToOne
    @JoinColumn(name="card_bin_id")
    CardBin cardBin

    @ManyToOne
    Bank bank

    @ManyToOne
    Client client

}
