package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.data.TimeComponents
import com.batoulapps.adhan.internal.SolarTime
import java.lang.Math.ceil
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Helper Class to calculate Aqrabul Ayyam
 */
class AqrabulAyyam(val coordinates: Coordinates) {

    /**
     * Recursively try to get Aqrabul Ayyam Date.
     */
    fun getLastTrueSunset(date: DateComponents): DateComponents {
        val solarTime = SolarTime(date, coordinates)
        val timeComponent = TimeComponents.fromDouble(solarTime.hourAngle(-18.0, false))
        if (timeComponent == null) {
            var setDate = LocalDate.of(date.year, date.month, date.day)
            setDate = setDate.minusDays(1)
            return getLastTrueSunset(DateComponents(setDate.year, setDate.month.value, setDate.dayOfMonth))
        } else {
            return date
        }
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
        println("Latitude is $latitude, Longitude is $longitude and fajrAQ is $fajrAQ which is $t mins")
        return time.plusMinutes(t.toLong())
    }

}
