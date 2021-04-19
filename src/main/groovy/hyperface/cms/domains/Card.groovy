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

    CardBin cardBin
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
    Boolean enableCashWithdrawl = false

    Double dailyCashWithdrawlLimit
    Double dailyTransactionLimit

    @ManyToOne
    @JoinColumn(name="credit_account_id")
    CreditAccount creditAccount
}
