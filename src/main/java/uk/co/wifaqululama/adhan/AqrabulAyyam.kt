package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.data.TimeComponents
import com.batoulapps.adhan.internal.SolarTime
import jdk.vm.ci.meta.Local
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
    fun getLastTrueSunset(date: DateComponents):DateComponents{
        val solarTime = SolarTime(date,coordinates)
        val timeComponent = TimeComponents.fromDouble(solarTime.hourAngle(-18.0, false))
        if(timeComponent == null){
            var setDate = LocalDate.of(date.year,date.month,date.day)
            setDate = setDate.minusDays(1)
            return getLastTrueSunset(DateComponents(setDate.year,setDate.month.value,setDate.dayOfMonth))
        } else{
            return date
        }
    }

    /**
     * Get Aqrabul Ayyam based on British pre-calculated averages. MUST only be used when in Britain
     */
    fun getBritishAqrabulAyyamTime(date: DateComponents): LocalTime {
        var time  = LocalTime.of(0,0,0);
        var fajrAQ: Double = 1.78
        val latitude = coordinates.latitude
        if(latitude > 48.89){
            fajrAQ = when{
                latitude > 49.89 -> 1.68
                latitude > 50 ->  1.7
                latitude > 50.25 -> 1.68
                latitude > 50.5 -> 1.45
                latitude > 50.75 -> 1.38
                latitude > 51 -> 1.48
                latitude > 51.25 -> 1.47
                latitude > 51.5 -> 1.43
                latitude > 51.75 -> 1.45
                latitude > 52 -> 1.52
                latitude > 52.25 -> 1.47
                latitude > 52.5 -> 1.37
                latitude > 52.75 -> 1.52
                latitude > 53 -> 1.47
                latitude > 53.25 -> 1.62
                latitude > 53.5 -> 1.57
                latitude > 53.75 -> 1.57
                latitude > 54 -> 1.6
                latitude > 54.5 -> 1.63
                latitude > 55 -> 1.82
                latitude > 55.5 -> 1.95
                latitude > 56 -> 1.92
                latitude > 56.5 -> 1.9
                latitude > 57 -> 2.03
                latitude > 57.5 -> 1.75
                latitude > 59 -> 1.75
                else -> {
                  1.78 // TODO return last second-to-last date and save that instead here.
                }
            }
        }
        val t = (100 * fajrAQ)
        val fajrOffsetMins = t / 100
        return time.plusMinutes(fajrOffsetMins.toLong())
    }
}