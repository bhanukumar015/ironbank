package hyperface.cms.commands

import hyperface.cms.domains.CreditAccount

import javax.validation.constraints.AssertTrue
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class FetchAccountStatementsRequest {
    String from
    String to

    // Derived fields
    ZonedDateTime fromDate
    ZonedDateTime toDate
    CreditAccount account

    @AssertTrue(message = "Invalid date range. Max range allowed is 180 days")
    private Boolean isDateRangeValid() {
        if(to != null & from == null){
            return false
        }
        toDate = (to != null) ? ZonedDateTime.parse(to) : ZonedDateTime.now()
        fromDate = (from != null) ? ZonedDateTime.parse(from) : toDate.minusMonths(1)
        if(ChronoUnit.DAYS.between(fromDate, toDate) > 90){
            return false
        }
        return true
    }
}
