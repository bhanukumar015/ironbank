package hyperface.cms.repository


import hyperface.cms.domains.CreditCardProgram
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardProgramRepository extends CrudRepository<CreditCardProgram, Long> {

}