package hyperface.cms.domains

import com.fasterxml.jackson.annotation.JsonIgnore
import hyperface.cms.Constants
import org.hibernate.annotations.GenericGenerator

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
    @JoinColumn(name = "card_bin_id")
    CardBin cardBin

    String switchCardId

    String lastFourDigits

    int cardExpiryMonth
    int cardExpiryYear

    Boolean physicallyIssued
    Boolean virtuallyIssued

    Boolean isPrimaryCard
    Constants.CardType cardType
    // The card id of the second card created as part of the Phygital duo
    String phygitalDuoCardId

    Boolean virtualCardActivated = false
    Boolean physicalCardActivated = false

    Boolean isLocked = false
    Boolean hotlisted = false

    Boolean isFirstPurchaseDone
    Boolean isFirstRepaymentDone

    @ManyToOne
    @JoinColumn(name = "card_control_id")
    CardControl cardControl

    // TODO: Resolve cyclic dependency
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "credit_account_id")
    CreditAccount creditAccount

    @ManyToOne
    @JoinColumn(name = "credit_card_program_id")
    CreditCardProgram cardProgram

    @ManyToOne
    Client client

    @JsonIgnore
    @ManyToOne
    Bank bank
}