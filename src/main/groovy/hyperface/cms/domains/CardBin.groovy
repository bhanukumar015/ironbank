package hyperface.cms.domains

import hyperface.cms.Constants
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class CardBin {
//    @GenericGenerator(name = "card_bin_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
//    @GeneratedValue(generator = "card_bin_id")
//    String id

    @Id
    String bin
    Constants.CardScheme cardScheme
    String category //enum?
    Constants.CardProgramType programType
    Constants.CardHost cardHost

    @ManyToOne
    Bank bank

}
