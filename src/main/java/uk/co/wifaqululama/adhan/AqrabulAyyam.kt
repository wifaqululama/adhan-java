package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.data.TimeComponents
import com.batoulapps.adhan.internal.SolarTime
import java.lang.Math.ceil
import java.time.*
import java.util.*

/**
 * Helper Class to calculate Aqrabul Ayyam
 */
class AqrabulAyyam(val coordinates: Coordinates, val parameters: CalculationParameters) {

    /**
     * Recursively try to get Aqrabul Ayyam Date.
     */
    fun getLastTrueSunset(date: DateComponents): DateComponents {
        if(!isSunsetAcheived(date)){
            var setDate = LocalDate.of(date.year, date.month, date.day)
            setDate = setDate.minusDays(1)
            return getLastTrueSunset(DateComponents(setDate.year, setDate.month.value, setDate.dayOfMonth))
        } else {
            return date
        }
    }

     fun isSunsetAcheived(date: DateComponents): Boolean{
        val solarTime = SolarTime(date,coordinates)
        val timeComponent = TimeComponents.fromDouble(solarTime.hourAngle(-18.0, false))
         return timeComponent != null
    }

    fun getLastFajrTime(date: DateComponents): Date {
        val lastFajrDate = getLastTrueSunset(date)
        val lastFajrLd = LocalDate.of(lastFajrDate.year,lastFajrDate.month,lastFajrDate.day)
        val fajr = fajrOnDate(lastFajrLd)
        return Date.from(fajr.atDate(lastFajrLd).atZone(ZoneId.systemDefault()).toInstant())
    }

    @Deprecated("Not an accurate representation of AQ")
    fun getAveragedTime(date: DateComponents): Date {
        val lastFajrDate = getLastTrueSunset(date)
        val lastFajrLocalDate = LocalDate.of(date.year,date.month,date.day)
        val yearlyTimesList = getAverageFromYear(lastFajrLocalDate) + getAverageFromYear(lastFajrLocalDate.minusYears(1)) + getAverageFromYear(lastFajrLocalDate.minusYears(2)) + getAverageFromYear(lastFajrLocalDate.minusYears(3))
        var averageFajr = averageTime(yearlyTimesList)
        println(averageFajr)
        val instant: Instant =
            averageFajr!!.atDate(lastFajrLocalDate).atZone(ZoneId.systemDefault()).toInstant()
        val time = Date.from(instant)
        return time
    }

    private fun getAverageFromYear(date:LocalDate): List<LocalTime> {
        // check if the current date is okay to use as a starting point
        var workingDate = date
        if(!isSunsetAcheived(DateComponents.from(date))){
            var testDate = date
            while(!isSunsetAcheived(DateComponents.from(testDate))){
                testDate = testDate.minusDays(1)
            }
            workingDate = testDate
       }
        return listOf(fajrOnDate(workingDate),fajrOnDate((workingDate.minusDays(1))),fajrOnDate(workingDate.minusDays(2)))
    }

    private fun fajrOnDate(localDate: LocalDate): LocalTime {
        var fajrOnDate = PrayerTimes(coordinates, DateComponents.from(localDate), parameters).fajr
        val time =  LocalDateTime.ofInstant(
            fajrOnDate.toInstant(),
            ZoneId.systemDefault()
        ).toLocalTime()
        println("Date is $localDate and the time is $time")
        return time
    }

    private fun averageTime(list: List<LocalTime>): LocalTime {
        var nanoSum: Long = 0
        for (time in list) {
            nanoSum += time.toNanoOfDay()
        }
        return LocalTime.ofNanoOfDay(nanoSum / (list.size))
    }

    /**
     * Get Aqrabul Ayyam based on British pre-calculated averages. MUST only be used when in Britain
     */
    fun getBritishAqrabulAyyamTime(date: DateComponents): LocalTime {
        var time = LocalTime.of(0, 0, 0);
        var fajrAQ = 1.78001
        val latitude = coordinates.latitude
        val longitude = coordinates.longitude
        if (latitude > 48.89) {
            fajrAQ = when {
                latitude > 49.89 && latitude <= 50 -> 1.68
                latitude > 50 && latitude <= 50.25 -> 1.7
                latitude > 50.25 && latitude <= 50.5 -> 1.68
                latitude > 50.5 && latitude <= 50.75 -> 1.45
                latitude > 50.75 && latitude <= 51 -> 1.38
                latitude > 51 && latitude <= 51.25 -> 1.48
                latitude > 51.25 && latitude <= 51.5 -> 1.383 // Tooting
                latitude > 51.5 && latitude <= 51.75 -> 1.383 //Ilford
                latitude > 51.75 && latitude <= 52 -> 1.383 //Walthamstow
                latitude > 52 && latitude <= 52.25 -> 1.50
                latitude > 52.25 && latitude <= 52.5 -> 1.483 //Birmingham
                latitude > 52.5 && latitude <= 52.75 && longitude > -1.6343 -> 1.5 // Leicester
                latitude > 52.5 && latitude <= 52.75 && longitude < -1.6343 -> 1.566 // Wolverhampton
                latitude > 52.75 && latitude <= 53 -> 1.50
                latitude > 53 && latitude <= 53.25 -> 1.47
                latitude > 53.25 && latitude <= 53.5 -> 1.633 //Manchester
                latitude > 53.5 && latitude <= 53.75 -> 1.53 //Blackburn
                latitude > 53.75 && latitude <= 54 -> 1.65 //Preston
                latitude > 54 && latitude <= 54.5 -> 1.55
                latitude > 54.5 && latitude <= 55 -> 1.63
                latitude > 55 && latitude <= 55.5 -> 1.82
                latitude > 55.5 && latitude <= 56 -> 1.716 //Edinburgh
                latitude > 56 && latitude <= 56.5 -> 1.92
                latitude > 56.5 && latitude <= 57 -> 1.9
                latitude > 57 && latitude <= 57.5 -> 2.03
                latitude > 57.5 && latitude <= 59 -> 1.83
                latitude > 59 -> 1.75
                else -> {
                    1.78 // Unreachable Code
                }
            }
        }
        val t = ceil((60 * fajrAQ))
        return time.plusMinutes(t.toLong())
    }

}
