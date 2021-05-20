package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.data.TimeComponents
import com.batoulapps.adhan.internal.SolarTime
import java.time.LocalDate
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
}