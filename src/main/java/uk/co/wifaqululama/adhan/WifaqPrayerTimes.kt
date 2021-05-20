package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Adapter for Adhan Prayer Times with Wifaqul Ulama Rules
 */
class WifaqPrayerTimes(val coordinates: Coordinates,val preferences: CalculationPreferences) {
    val formatter = SimpleDateFormat("HH:mm")

    fun getPrayerTimes(date: Date): HashMap<Prayer,Date>{
        val dateComponent = DateComponents.from(date)
        val prayerTimesList = ArrayList<Date>()
        val prayerTimesMap = HashMap<Prayer,Date>()
        // Get and Calculate Prayer Times here...
        // Initial Calculation
        val params = WifaqDefaultCalculationParams.getCalculationParams()
        params.madhab = preferences.madhab
        val defaultTimes = PrayerTimes(coordinates,dateComponent,params)
        // Add initial (non-HighLat times)
        prayerTimesMap.put(Prayer.FAJR,defaultTimes.fajr)
        prayerTimesMap.put(Prayer.SUNRISE,defaultTimes.sunrise)
        prayerTimesMap.put(Prayer.DHUHR,defaultTimes.dhuhr)
        prayerTimesMap.put(Prayer.ASR,defaultTimes.asr)
        prayerTimesMap.put(Prayer.MAGHRIB,defaultTimes.maghrib)
        prayerTimesMap.put(Prayer.ISHA,defaultTimes.isha)

        when(preferences.highLatFajr){
            HighLatFajr.NISFUL_LAYL ->{
                // Calculate HighLat Fajr
                val c = Calendar.getInstance()
                c.time = date
                c.add(Calendar.DATE, 1) // number of days to add
                val nextDate = c.time
                val nextDayPrayerTimes = PrayerTimes(coordinates,DateComponents.from(nextDate),params)
                val sunrise = LocalTime.parse(formatter.format(nextDayPrayerTimes.sunrise))
                val sunset = LocalTime.parse(formatter.format(prayerTimesMap.get(Prayer.MAGHRIB)))
                val todaySunset = LocalDateTime.of(convertToLocalDateViaInstant(date),sunset)
                val tomorrowSunrise = LocalDateTime.of(convertToLocalDateViaInstant(nextDate),sunrise)
                var mins = todaySunset.until(tomorrowSunrise,ChronoUnit.HOURS)
                //mins += 1440/60
                println("nisful the number of hours in between is $mins")
                // Divide in two
                var nisfulMins = (mins/2).toInt()
                nisfulMins = Math.abs(nisfulMins)
                println("nisfulMins are $nisfulMins")
                c.time = date
                c.add(Calendar.MINUTE,nisfulMins);
                val nisfulFajr = c.time
                prayerTimesMap.put(Prayer.FAJR,nisfulFajr)
                //TODO determine if the Nisful Time should be used over the set Fajr Time
            }
            HighLatFajr.AQRABUL_AYYAM ->{
                //TODO determine last true 18deg time
            }
        }
        // Get High-Lat Isha Time and compare
        when(preferences.highLatIsha){
            HighLatIsha.HARAJ -> {
                // Get Haraj Isha Time
                params.highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
                val times = PrayerTimes(coordinates,dateComponent,params)
                if(times.isha.before(prayerTimesMap.get(Prayer.ISHA))){
                    prayerTimesMap.put(Prayer.ISHA,times.isha)
                }
            }
            HighLatIsha.AL_ABYADH ->{
                //TODO figure out how al_abyadh is calculated
            }
            HighLatIsha.AL_AHMAR ->{
                params.ishaAngle = 15.0
                val times = PrayerTimes(coordinates,dateComponent,params)
                prayerTimesMap.put(Prayer.ISHA,times.isha)
            }
        }
        return prayerTimesMap
    }

    fun convertToLocalDateViaInstant(dateToConvert: Date): LocalDate? {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}