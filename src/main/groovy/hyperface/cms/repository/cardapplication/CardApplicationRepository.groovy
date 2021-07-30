package hyperface.cms.repository.cardapplication


import hyperface.cms.domains.cardapplication.CardApplication
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardApplicationRepository extends CrudRepository<CardApplication, String> {
    @Query("select ca from CardApplication ca inner join DemographicDetail dd on ca.applicantDetails=dd.id where ca.clientId = ?1 and ca.programId = ?2 and dd.mobileNumber = ?3")
    CardApplication findApplicationByMobileClientAndProgram(String clientId, String programId, String mobile)
}
