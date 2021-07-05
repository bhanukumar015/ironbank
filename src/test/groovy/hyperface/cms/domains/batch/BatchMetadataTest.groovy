package hyperface.cms.domains.batch

import hyperface.cms.repository.batch.BatchMetadataRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BatchMetadataTest {
    @Autowired
    private BatchMetadataRepository batchMetadataRepository

    @Test
    @Order(1)
    void entityTest() {
        Assertions.assertTrue(batchMetadataRepository.count() >= 0, "The batch_metadata table must exist.");
    }

    @Test
    @Order(2)
    void insertionTest() {
        BatchMetadata batchMetadata = new BatchMetadata()
        ZonedDateTime t1 = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        batchMetadata.tap {
            batchName = BatchMetadata.BatchName.CURRENCY_CONVERSION
            source = BatchMetadata.Source.NIUM
            lastUpdated = t1
        }
        BatchMetadata result = batchMetadataRepository.save(batchMetadata)
        Assertions.assertTrue(result.source == BatchMetadata.Source.NIUM)
        Assertions.assertTrue(result.batchName == BatchMetadata.BatchName.CURRENCY_CONVERSION)
        Assertions.assertTrue(result.getLastUpdated().withZoneSameLocal(ZoneId.of("UTC+0530")) == t1)

        ZonedDateTime t2 = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        batchMetadata.tap {
            lastUpdated = t2
        }
        result = batchMetadataRepository.save(batchMetadata)
        Assertions.assertTrue(result.source == BatchMetadata.Source.NIUM)
        Assertions.assertTrue(result.batchName == BatchMetadata.BatchName.CURRENCY_CONVERSION)
        Assertions.assertTrue(result.getLastUpdated().withZoneSameLocal(ZoneId.of("UTC+0530")) == t2)
    }

    @Test
    @Order(3)
    void retrievalWithUpdateTest() {
        BatchMetadata batchMetadata = new BatchMetadata()
        batchMetadata.tap {
            batchName = BatchMetadata.BatchName.CURRENCY_CONVERSION
            source = BatchMetadata.Source.VISA
            lastUpdated = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        }
        batchMetadataRepository.save(batchMetadata)

        ZonedDateTime lastTimeUpdated = ZonedDateTime.now(ZoneId.of("UTC+0530"))
        batchMetadata.tap {
            source = BatchMetadata.Source.NIUM
            lastUpdated = lastTimeUpdated
        }
        batchMetadataRepository.save(batchMetadata)

        Optional<BatchMetadata> result = batchMetadataRepository.findById(BatchMetadata.BatchName.CURRENCY_CONVERSION)
        Assertions.assertTrue(result.isPresent())
        Assertions.assertTrue(result.get().getSource() == BatchMetadata.Source.NIUM)
        ZonedDateTime resTime = result.get().getLastUpdated().withZoneSameLocal(ZoneId.of("UTC+0530"))
        Assertions.assertTrue(resTime.getDayOfMonth() == lastTimeUpdated.getDayOfMonth())
        Assertions.assertTrue(resTime.getMonth() == lastTimeUpdated.getMonth())
        Assertions.assertTrue(resTime.getYear() == lastTimeUpdated.getYear())
        Assertions.assertTrue(resTime.getHour() == lastTimeUpdated.getHour())
        Assertions.assertTrue(resTime.getMinute() == lastTimeUpdated.getMinute())
        Assertions.assertTrue(resTime.getSecond() - lastTimeUpdated.getSecond() <= 1)
    }
}
