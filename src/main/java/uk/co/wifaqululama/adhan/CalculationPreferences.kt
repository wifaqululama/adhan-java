package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.Madhab

data class CalculationPreferences(val madhab: Madhab = Madhab.HANAFI, val highLatFajr: HighLatFajr = HighLatFajr.AQRABUL_AYYAM, val highLatIsha: HighLatIsha = HighLatIsha.HARAJ )