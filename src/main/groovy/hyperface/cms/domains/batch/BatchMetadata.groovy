package hyperface.cms.domains.batch


import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import java.time.ZonedDateTime

@Entity
class BatchMetadata {
    @Id
    @Enumerated(EnumType.STRING)
    BatchName batchName

    @Enumerated(EnumType.STRING)
    Source source

    ZonedDateTime lastUpdated

}
