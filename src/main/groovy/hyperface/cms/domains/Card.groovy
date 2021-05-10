package hyperface.cms.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Card implements PaymentInstrument {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    @ManyToOne
    @JoinColumn(name="card_bin_id")
    CardBin cardBin

    String lastFourDigits

    int cardExpiryMonth
    int cardExpiryYear

    Boolean physicallyIssued
    Boolean virtuallyIssued

    Boolean virtualCardActivatedByCustomer = false
    Boolean physicalCardActivatedByCustomer = false

    Boolean cardSuspendedByCustomer = false
    Boolean enableOverseasTransactions = false
    Boolean enableDomesticTransactions = false
    Boolean enableNFC = false
    Boolean enableOnlineTransactions = false
    Boolean enableCashWithdrawal = false

    Double dailyCashWithdrawalLimit
    Double dailyTransactionLimit

    Boolean hotlisted = false

    @ManyToOne
    @JoinColumn(name="credit_account_id")
    CreditAccount creditAccount

    @ManyToOne
    CardProgram cardProgram
}
