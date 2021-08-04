package hyperface.cms.commands

import hyperface.cms.domains.CreditAccount

import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class FetchAccountTransactionsRequest {
    @Positive(message = "Count must be a positive integer")
    Integer count = 10
    @PositiveOrZero(message = "Offset must be a non-negative integer")
    Integer offset = 0
    String from
    String to

    // Derived fields
    ZonedDateTime fromDate
    ZonedDateTime toDate
    CreditAccount account

    @AssertTrue(message = "Invalid date range. Max range allowed is 90 days")
    private Boolean isDateRangeValid(){
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
