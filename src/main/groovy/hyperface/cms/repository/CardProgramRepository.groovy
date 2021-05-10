package hyperface.cms.repository

import hyperface.cms.domains.CardBin
import hyperface.cms.domains.CardProgram
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardProgramRepository extends CrudRepository<CardProgram, Long> {

}