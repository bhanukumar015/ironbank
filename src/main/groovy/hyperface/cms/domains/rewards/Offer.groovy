package hyperface.cms.domains.rewards

import hyperface.cms.domains.CreditCardProgram
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
class Offer {
    enum OfferType {
        REWARDS,
        CASHBACK,
        FESTIVE_OFFER
    }
    @Id
    @GenericGenerator(name = "offer_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "offer_id")
    String id

    @Enumerated(EnumType.STRING)
    OfferType offerType

    @ManyToOne(optional = false)
    @JoinColumn(name = "credit_card_program_id", referencedColumnName = "id")
    CreditCardProgram creditCardProgram
}
