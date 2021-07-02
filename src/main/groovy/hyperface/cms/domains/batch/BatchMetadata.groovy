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
    BatchMetadata.BatchName batchName

    @Enumerated(EnumType.STRING)
    BatchMetadata.Source source

    ZonedDateTime lastUpdated

    static enum BatchName {
        CURRENCY_CONVERSION;
    }

    static enum Source {
        VISA,
        NIUM;
    }

}

