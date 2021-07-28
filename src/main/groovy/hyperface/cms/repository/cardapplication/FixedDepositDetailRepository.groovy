package hyperface.cms.repository.cardapplication


import hyperface.cms.domains.cardapplication.FixedDepositDetail
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FixedDepositDetailRepository extends CrudRepository<FixedDepositDetail, String> {
}
