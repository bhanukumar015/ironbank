package hyperface.cms.cron

import groovy.util.logging.Slf4j
import hyperface.cms.domains.CreditAccount
import hyperface.cms.service.CreditAccountService
import hyperface.cms.util.MyDateUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.ZonedDateTime

@Component
@Slf4j
class StatementGenerationCron {

    @Autowired
    CreditAccountService creditAccountService;

    /**
     * If Bill Cycle date is 21,
     * Statement should be generated on 22nd after taking into account all transactions received until 21st 23:59
     */
    void statementGenerationCron() {
        // Any Billing cycle between previous day start time and less than current day start time
        Date previousDay = MyDateUtil.addXDaysToCurrentTime(-1)
        previousDay = MyDateUtil.resetDateToStartOrEndOfTheDay(previousDay, true)
        ZonedDateTime previousDayStartDateTime = MyDateUtil.convertFromDateToZonedDateTime(previousDay)
        Date currentDay = Calendar.getInstance().getTime()
        currentDay = MyDateUtil.resetDateToStartOrEndOfTheDay(currentDay, true)
        ZonedDateTime currentDayStartDateTime = MyDateUtil.convertFromDateToZonedDateTime(currentDay)
        log.info("Statement Generation for all accounts whose billing cycle is between ${previousDayStartDateTime} and ${currentDayStartDateTime}")
        List<CreditAccount> creditAccounts = creditAccountService.getAllCreditAccountsBillingCycleEndDate(previousDayStartDateTime, currentDayStartDateTime)
        println creditAccounts;
    }
}
