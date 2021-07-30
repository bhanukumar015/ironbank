package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import java.time.ZonedDateTime

@Entity
class CardToken {

    enum TokenStatus {ACTIVATED, SUSPENDED, DEACTIVATED, UNKNOWN}

    @Id
    @GenericGenerator(name = "token_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "token_id")
    String id

    Date expiryDate

    String tokenNumber

    String requestor

    String referenceNumber

    String deviceId

    String deviceType

    ZonedDateTime createdOn

    ZonedDateTime updatedOn

    @Enumerated(EnumType.STRING)
    TokenStatus status

    @ManyToOne
    Card card
}
