package hyperface.cms.domains

import hyperface.cms.Constants

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class CardBin {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    String bin
    String bank
    Constants.CardScheme cardScheme
    String category //enum?
    Constants.CardProgramType programType
    Constants.CardHost cardHost

}
