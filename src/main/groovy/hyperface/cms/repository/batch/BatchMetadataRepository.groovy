package hyperface.cms.repository.batch

import hyperface.cms.domains.batch.BatchMetadata

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BatchMetadataRepository extends CrudRepository<BatchMetadata, BatchMetadata.BatchName> {

}