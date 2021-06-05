package hyperface.cms.domains

import hyperface.cms.Constants
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne

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

    @ManyToOne
    CreditCardScheduleOfCharges scheduleOfCharges

    @ManyToOne
    CardBin cardBin

    @ManyToOne
    Bank bank

    @ManyToOne
    Client client

}
