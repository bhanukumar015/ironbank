package hyperface.cms.util

import org.apache.commons.lang3.time.DateUtils

import java.time.ZoneId
import java.time.ZonedDateTime

class MyDateUtil {

    static Date getLastMonthBeginning() {
        Calendar calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.getTime()
    }

    static Date addXDaysToCurrentTime(int days) {
        Calendar cal = Calendar.getInstance()
        cal.add(Calendar.DATE, days)
        return cal.getTime()
    }

    static Date resetDateToStartOrEndOfTheDay(Date date, boolean startOfTheDay) {
        Date convertedDate = DateUtils.setHours(date, startOfTheDay ? 0 : 23);
        convertedDate = DateUtils.setMinutes(convertedDate, startOfTheDay ? 0 : 59);
        convertedDate = DateUtils.setSeconds(convertedDate, startOfTheDay ? 0 : 59);
        convertedDate = DateUtils.setMilliseconds(convertedDate, startOfTheDay ? 0 : 999);
        return convertedDate;
    }

    static ZonedDateTime convertFromDateToZonedDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.of("UTC+0530"))
    }
}
