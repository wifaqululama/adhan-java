package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.data.TimeComponents
import com.batoulapps.adhan.internal.SolarTime
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
    var isUK = false

    /**
     * Secondary Constructor that can be used to flag if UK Aqrabul Ayyam Calculations can be used
     */
    constructor(coordinates: Coordinates, preferences: CalculationPreferences, isUK: Boolean) : this(coordinates,preferences) {
        this.isUK = true
    }
    val formatter = SimpleDateFormat("HH:mm")

    fun getPrayerTimes(date: LocalDate): HashMap<Prayer,Date>{
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
//                if(isUK){
//                    val fajrAQ = aqrabulAyyam.getBritishAqrabulAyyamTime(dateComponent)
//                    val mInstant = fajrAQ.atDate(date).atZone(ZoneId.systemDefault()).toInstant()
//                    val dateFajr = Date.from(mInstant)
//                    if(dateFajr.time > prayerTimesMap.get(Prayer.FAJR)!!.time )
//                        prayerTimesMap.put(Prayer.FAJR,dateFajr)
//                } else{
//                    val newDate = aqrabulAyyam.getLastTrueSunset(dateComponent)
//                    val newTimes = PrayerTimes(coordinates, newDate, params)
//                    prayerTimesMap.put(Prayer.FAJR,newTimes.fajr)
//                }
                if(!aqrabulAyyam.isSunsetAcheived(dateComponent)){
                    val avgTime = aqrabulAyyam.getAveragedTime(dateComponent)
                    prayerTimesMap.put(Prayer.FAJR, avgTime)
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
                //TODO figure out how al_abyadh is calculated
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

    private fun getHarajTime(date: DateComponents,calculationParameters: CalculationParameters){

    }

    fun convertToLocalDateViaInstant(dateToConvert: Date): LocalDate? {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}