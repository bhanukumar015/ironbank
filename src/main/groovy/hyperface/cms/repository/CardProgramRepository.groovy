package hyperface.cms.repository


import hyperface.cms.domains.CreditCardProgram
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface CardProgramRepository extends CrudRepository<CreditCardProgram, String> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE CreditCardProgram cp SET cp.currentDayAccountCount = 0, isActive = CASE WHEN disableLevel = 'DAILY' THEN true ELSE isActive END, disableLevel = CASE WHEN disableLevel = 'DAILY' THEN null ELSE disableLevel END")
    void resetDailyAccountCount();

    @Modifying
    @Transactional
    @Query(value = "UPDATE CreditCardProgram cp SET cp.currentWeekAccountCount = 0, isActive = CASE WHEN disableLevel = 'WEEKLY' THEN true ELSE isActive END, disableLevel = CASE WHEN disableLevel = 'WEEKLY' THEN null ELSE disableLevel END")
    void resetWeeklyAccountCount();

    @Modifying
    @Transactional
    @Query(value = "UPDATE CreditCardProgram cp SET cp.currentMonthAccountCount = 0, isActive = CASE WHEN disableLevel = 'MONTHLY' THEN true ELSE isActive END, disableLevel = CASE WHEN disableLevel = 'MONTHLY' THEN null ELSE disableLevel END")
    void resetMonthlyAccountCount();
}