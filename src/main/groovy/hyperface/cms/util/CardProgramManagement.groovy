package hyperface.cms.util

import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.repository.CardProgramRepository
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CardProgramManagement {

    @Autowired
    CardProgramRepository cardProgramRepository

    private Logger log = LoggerFactory.getLogger(CardProgramManagement.class)

    @Async
    public void updateCardProgramCounts(CreditCardProgram cardProgram){

        def disableProgram = {count, limit, level ->
            if(count >= limit){
                cardProgram.isActive = false
                cardProgram.disableLevel = level
            }
        }

        disableProgram(++cardProgram.currentDayAccountCount, cardProgram.dailyAccountLimit
                , CreditCardProgram.DisableLevel.DAILY)
        disableProgram(++cardProgram.currentWeekAccountCount, cardProgram.weeklyAccountLimit
                , CreditCardProgram.DisableLevel.WEEKLY)
        disableProgram(++cardProgram.currentMonthAccountCount, cardProgram.monthlyAccountLimit
                , CreditCardProgram.DisableLevel.MONTHLY)
        disableProgram(++cardProgram.lifetimeAccountCount, cardProgram.lifetimeAccountLimit
                , CreditCardProgram.DisableLevel.LIFETIME)

        cardProgramRepository.save(cardProgram)
    }

    // Scheduled to run at 00:00 everyday. Resets the daily count for account created
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "DailyAccountCountReset", lockAtLeastFor = "PT23h55m", lockAtMostFor = "PT23h55m")
    void resetDailyAccountCount(){
        log.info("Resetting daily account created count at ${new Date()}")
        cardProgramRepository.resetDailyAccountCount()
    }

    // Scheduled to run at 00:00 on Sundays. Resets the weekly count for account created
    @Scheduled(cron = "0 0 0 * * SUN")
    @SchedulerLock(name = "WeeklyAccountCountReset", lockAtLeastFor = "P6dT23h55m", lockAtMostFor = "P6dT23h55m")
    void resetWeeklyAccountCount(){
        log.info("Resetting weekly account created count at ${new Date()}")
        cardProgramRepository.resetWeeklyAccountCount()
    }

    // Scheduled to run at 00:00 on the first of the month. Resets the monthly count for account created
    @Scheduled(cron = "0 0 0 1 * *")
    @SchedulerLock(name = "MonthlyAccountCountReset", lockAtLeastFor = "P27dT23h55m", lockAtMostFor = "P27dT23h55m")
    void resetMonthlyAccountCount(){
        log.info("Resetting monthy account created count at ${new Date()}")
        cardProgramRepository.resetMonthlyAccountCount()
    }
}
