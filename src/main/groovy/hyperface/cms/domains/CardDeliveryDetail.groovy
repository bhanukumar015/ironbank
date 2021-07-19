package hyperface.cms.domains

import hyperface.cms.domains.converters.SimpleJsonConverter
import hyperface.cms.model.enums.CardDeliveryStatus
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import java.time.ZonedDateTime

@Entity
class CardDeliveryDetail {
    @Id
    @GenericGenerator(name = "card_delivery_detail_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "card_delivery_detail_id")
    String id

    String waybillNumber

    String courierPartnerId

    ZonedDateTime expectedDeliveryDate

    ZonedDateTime deliveredOn

    @Convert(converter = SimpleJsonConverter.class)
    Scan latestStatus

    @Convert(converter = SimpleJsonConverter.class)
    List<Scan> scans

    @OneToOne
    @JoinColumn(name = "card_id", referencedColumnName = "id")
    Card card
}

class Scan{
    ZonedDateTime timestamp

    @Enumerated(EnumType.STRING)
    CardDeliveryStatus status

    String statusDescription

    String remarks

    String location
}