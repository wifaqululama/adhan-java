package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.data.TimeComponents
import com.batoulapps.adhan.internal.SolarTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Adapter for Adhan Prayer Times with Wifaqul Ulama Rules
 */
class WifaqPrayerTimes(val coordinates: Coordinates, val preferences: CalculationPreferences) {
    var isUK = false

    /**
     * Secondary Constructor that can be used to flag if UK Aqrabul Ayyam Calculations can be used
     */
    constructor(coordinates: Coordinates, preferences: CalculationPreferences, isUK: Boolean) : this(
        coordinates, preferences
    ) {
        this.isUK = true
    }

    val formatter = SimpleDateFormat("HH:mm")

    fun getPrayerTimes(date: LocalDate): HashMap<Prayer, Date> {
        val dateComponent = DateComponents.from(date)
        // Get and Calculate Prayer Times here...
        // Initial Calculation
        val params = WifaqDefaultCalculationParams.getCalculationParams()
        params.madhab = preferences.madhab
        val defaultTimes = PrayerTimes(coordinates, dateComponent, params)
        val prayerTimesMap = getTimesInMap(defaultTimes)
        overwriteWithWifaqOverrides(params, dateComponent, prayerTimesMap)
        return prayerTimesMap
    }

    fun getCurrentPrayer(): SingularPrayer {
        val now = LocalDateTime.now()
        val todayTimes = getPrayerTimes(now.toLocalDate())
        val `when`: Long = Date().time
        var toReturn: SingularPrayer = SingularPrayer(Prayer.FAJR, Date())
        if (todayTimes[Prayer.ISHA]!!.getTime() - `when` <= 0) {
            toReturn = SingularPrayer(Prayer.ISHA, todayTimes[Prayer.ISHA]!!)
        } else if (todayTimes[Prayer.MAGHRIB]!!.getTime() - `when` <= 0) {
            toReturn = SingularPrayer(Prayer.MAGHRIB, todayTimes[Prayer.MAGHRIB]!!)
        } else if (todayTimes[Prayer.ASR]!!.getTime() - `when` <= 0) {
            toReturn =SingularPrayer(Prayer.ASR, todayTimes[Prayer.ASR]!!)
        } else if (todayTimes[Prayer.DHUHR]!!.getTime() - `when` <= 0) {
            toReturn = SingularPrayer(Prayer.DHUHR, todayTimes[Prayer.DHUHR]!!)
        } else if (todayTimes[Prayer.SUNRISE]!!.getTime() - `when` <= 0) {
            toReturn = SingularPrayer(Prayer.SUNRISE, todayTimes[Prayer.SUNRISE]!!)
        } else if (todayTimes[Prayer.FAJR]!!.getTime() - `when` <= 0) {
            toReturn = SingularPrayer(Prayer.FAJR, todayTimes[Prayer.FAJR]!!)
        }
        return toReturn
    }

    fun getNextPrayerMap(): Map<Prayer, Date> {
        val now = LocalDateTime.now()
        val next = LocalDateTime.now().plusDays(1)
        val todayTimes = getPrayerTimes(now.toLocalDate())
        val tomorrowTimes = getPrayerTimes(next.toLocalDate())
        val times = HashMap<Prayer, Date>()
         todayTimes.entries.forEach {
            if(now.isAfter(convertToLocalDateTimeViaInstant(it.value))) {
                times[it.key] = tomorrowTimes[it.key]!!
            } else {
                times[it.key] = it.value
            }
        }
        return times
    }

    fun getNextPrayer(): SingularPrayer {
        val nextPrayerMap = getNextPrayerMap()
        val now = LocalDateTime.now()
        val sorted = nextPrayerMap.values.sorted()[0]
        return SingularPrayer(getKey(nextPrayerMap, sorted)!!, sorted)
    }

    private fun <K, V> getKey(map: Map<K, V>, target: V): K? {
        for (key in map.keys)
        {
            if (target == map[key]) {
                return key
            }
        }
        return null
    }

    private fun getTimesInMap(defaultTimes: PrayerTimes): HashMap<Prayer, Date> {
        val prayerTimesMap = HashMap<Prayer, Date>()
        prayerTimesMap[Prayer.FAJR] = defaultTimes.fajr
        prayerTimesMap[Prayer.SUNRISE] = defaultTimes.sunrise
        prayerTimesMap[Prayer.DHUHR] = defaultTimes.dhuhr
        prayerTimesMap[Prayer.ASR] = defaultTimes.asr
        prayerTimesMap[Prayer.MAGHRIB] = defaultTimes.maghrib
        prayerTimesMap[Prayer.ISHA] = defaultTimes.isha
        return prayerTimesMap
    }

    private fun overwriteWithWifaqOverrides(
        params: CalculationParameters, dateComponent: DateComponents, prayerTimesMap: HashMap<Prayer, Date>
    ) {
        when (preferences.highLatFajr) {
            HighLatFajr.NISFUL_LAYL -> calculateNisfulLayl(params, dateComponent, prayerTimesMap)
            HighLatFajr.AQRABUL_AYYAM -> calculateAqrabulAyyam(params, dateComponent, prayerTimesMap)
        }
        // Get High-Lat Isha Time and compare
        when (preferences.highLatIsha) {
            HighLatIsha.HARAJ -> calculateHarajTime(params, dateComponent, prayerTimesMap)
            HighLatIsha.AL_ABYADH -> calculateIshaWithAngle(params, dateComponent, prayerTimesMap, 18.0)
            HighLatIsha.AL_AHMAR -> calculateIshaWithAngle(params, dateComponent, prayerTimesMap, 15.0)
        }
    }

    private fun calculateNisfulLayl(
        params: CalculationParameters, dateComponent: DateComponents?, prayerTimesMap: HashMap<Prayer, Date>
    ) {
        if (params.highLatitudeRule != HighLatitudeRule.MIDDLE_OF_THE_NIGHT) {
            // GET NISFUL LAYL Time
            params.highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT
            val midnightTimes = PrayerTimes(coordinates, dateComponent, params)
            prayerTimesMap.put(Prayer.FAJR, midnightTimes.fajr)
        }
    }

    private fun calculateAqrabulAyyam(
        params: CalculationParameters, dateComponent: DateComponents, prayerTimesMap: HashMap<Prayer, Date>
    ) {
        val aqrabulAyyam = AqrabulAyyam(coordinates, params)
        if (!aqrabulAyyam.isSunsetAcheived(dateComponent)) {
            val avgTime = aqrabulAyyam.getAveragedTime(dateComponent)
            prayerTimesMap.put(Prayer.FAJR, avgTime)
        }
    }

    private fun calculateIshaWithAngle(
        params: CalculationParameters,
        dateComponent: DateComponents,
        prayerTimesMap: HashMap<Prayer, Date>,
        angle: Double
    ) {
        params.ishaAngle = angle
        if (!ishaAnglePossible(dateComponent, -angle)) {
            params.highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
        }
        val times = PrayerTimes(coordinates, dateComponent, params)
        prayerTimesMap.put(Prayer.ISHA, times.isha)
    }

    private fun calculateHarajTime(
        params: CalculationParameters, dateComponent: DateComponents?, prayerTimesMap: HashMap<Prayer, Date>
    ) {
        // Get Haraj Isha Time
        params.highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
        val times = PrayerTimes(coordinates, dateComponent, params)
        if (times.isha.before(prayerTimesMap.get(Prayer.ISHA))) {
            prayerTimesMap.put(Prayer.ISHA, times.isha)
        }
    }


    private fun ishaAnglePossible(date: DateComponents, angle: Double): Boolean {
        val solarTime = SolarTime(date, coordinates)
        val timeComponent = TimeComponents.fromDouble(solarTime.hourAngle(angle, false))
        return timeComponent != null
    }

    private fun getHarajTime(date: DateComponents, calculationParameters: CalculationParameters) {

    }

    private fun convertToLocalDateViaInstant(dateToConvert: Date): LocalDate? {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun convertToLocalDateTimeViaInstant(dateToConvert: Date): LocalDateTime =
        dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

data class SingularPrayer(val prayer: Prayer, val date: Date)