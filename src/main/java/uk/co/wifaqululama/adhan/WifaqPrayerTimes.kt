package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.data.TimeComponents
import com.batoulapps.adhan.internal.SolarTime
import java.time.LocalDate
import java.util.*

/**
 * Adapter for Adhan Prayer Times with Wifaqul Ulama Rules
 */
class WifaqPrayerTimes(val coordinates: Coordinates,val preferences: CalculationPreferences) {

    fun getPrayerTimes(date: LocalDate): HashMap<Prayer,Date>{
        val dateComponent = DateComponents.from(date)
        val prayerTimesMap = HashMap<Prayer,Date>()
        // Get and Calculate Prayer Times here...
        // Initial Calculation
        val params = WifaqDefaultCalculationParams.getCalculationParams()
        params.madhab = preferences.madhab
        val defaultTimes = PrayerTimes(coordinates,dateComponent,params)
        // Add initial (non-HighLat times)
        prayerTimesMap[Prayer.FAJR] = defaultTimes.fajr
        prayerTimesMap[Prayer.SUNRISE] = defaultTimes.sunrise
        prayerTimesMap[Prayer.DHUHR] = defaultTimes.dhuhr
        prayerTimesMap[Prayer.ASR] = defaultTimes.asr
        prayerTimesMap[Prayer.MAGHRIB] = defaultTimes.maghrib
        prayerTimesMap[Prayer.ISHA] = defaultTimes.isha

        when(preferences.highLatFajr){
            HighLatFajr.NISFUL_LAYL ->{
                // DEFAULT OPTION FOR FAJR
                if(!(params.highLatitudeRule == HighLatitudeRule.MIDDLE_OF_THE_NIGHT)){
                    // GET NISFUL LAYL Time
                    params.highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT
                    val midnightTimes = PrayerTimes(coordinates,dateComponent,params)
                    prayerTimesMap.put(Prayer.FAJR,midnightTimes.fajr)
                }
            }
            HighLatFajr.AQRABUL_AYYAM -> {
                //TODO determine last true 18deg time
                val aqrabulAyyam = AqrabulAyyam(coordinates,params)
                if(!aqrabulAyyam.isSunsetAcheived(dateComponent)){
                    val aqrabulAyyamTime = aqrabulAyyam.getLastFajrTime(dateComponent)
                    prayerTimesMap[Prayer.FAJR] = aqrabulAyyamTime
                }

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
                params.ishaAngle = 18.0
                if(!ishaAnglePossible(dateComponent,-18.0)){
                    params.highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
                }
                val times = PrayerTimes(coordinates,dateComponent,params)
                prayerTimesMap.put(Prayer.ISHA,times.isha)
            }
            HighLatIsha.AL_AHMAR ->{
                params.ishaAngle = 15.0
                if(!ishaAnglePossible(dateComponent,-15.0)){
                    params.highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
                }
                val times = PrayerTimes(coordinates,dateComponent,params)
                prayerTimesMap.put(Prayer.ISHA,times.isha)
            }
        }
        return prayerTimesMap
    }

    private fun ishaAnglePossible(date: DateComponents,angle:Double): Boolean {
        val solarTime = SolarTime(date,coordinates)
        val timeComponent = TimeComponents.fromDouble(solarTime.hourAngle(angle, false))
        return timeComponent !=null
    }
}