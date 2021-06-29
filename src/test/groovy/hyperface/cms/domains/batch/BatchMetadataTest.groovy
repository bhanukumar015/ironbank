package hyperface.cms.domains.batch

import hyperface.cms.repository.batch.BatchMetadataRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest
class BatchMetadataTest {
    @Autowired
    private BatchMetadataRepository batchMetadataRepository

    @Test
    void entityTest() {
        Assertions.assertTrue(batchMetadataRepository.count() >= 0, "The batch_metadata table must exist.");
    }

    @Test
    void insertionTest() {
        BatchMetadata batchMetadata = new BatchMetadata()
        ZonedDateTime t1 = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        batchMetadata.tap {
            batchName = BatchName.CURRENCY_CONVERSION
            source = Source.NIUM
            lastUpdated = t1
        }
        BatchMetadata result = batchMetadataRepository.save(batchMetadata)
        Assertions.assertTrue(result.source == Source.NIUM)
        Assertions.assertTrue(result.batchName == BatchName.CURRENCY_CONVERSION)
        Assertions.assertTrue(result.getLastUpdated().withZoneSameLocal(ZoneId.of("UTC+0530")) == t1)

        ZonedDateTime t2 = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        batchMetadata.tap {
            lastUpdated = t2
        }
        result = batchMetadataRepository.save(batchMetadata)
        Assertions.assertTrue(result.source == Source.NIUM)
        Assertions.assertTrue(result.batchName == BatchName.CURRENCY_CONVERSION)
        Assertions.assertTrue(result.getLastUpdated().withZoneSameLocal(ZoneId.of("UTC+0530")) == t2)
    }

    @Test
    void retrievalWithUpdateTest() {
        BatchMetadata batchMetadata = new BatchMetadata()
        batchMetadata.tap {
            batchName = BatchName.CURRENCY_CONVERSION
            source = Source.VISA
            lastUpdated = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        }
        batchMetadataRepository.save(batchMetadata)

        ZonedDateTime lastTimeUpdated = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        batchMetadata.tap {
            source = Source.NIUM
            lastUpdated = lastTimeUpdated
        }
        batchMetadataRepository.save(batchMetadata)

        Optional<BatchMetadata> result = batchMetadataRepository.findById(BatchName.CURRENCY_CONVERSION)
        Assertions.assertTrue(result.isPresent())
        Assertions.assertTrue(result.get().getSource() == Source.NIUM)
        ZonedDateTime resTime = result.get().getLastUpdated().withZoneSameLocal(ZoneId.of("UTC+0530"))
        Assertions.assertTrue(resTime.getDayOfMonth() == lastTimeUpdated.getDayOfMonth())
        Assertions.assertTrue(resTime.getMonth() == lastTimeUpdated.getMonth())
        Assertions.assertTrue(resTime.getYear() == lastTimeUpdated.getYear())
        Assertions.assertTrue(resTime.getHour() == lastTimeUpdated.getHour())
        Assertions.assertTrue(resTime.getMinute() == lastTimeUpdated.getMinute())
        Assertions.assertTrue(resTime.getSecond() == lastTimeUpdated.getSecond())
    }
}
