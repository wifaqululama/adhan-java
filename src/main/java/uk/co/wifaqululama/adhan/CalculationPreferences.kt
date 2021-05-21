package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.Madhab

data class CalculationPreferences(var madhab: Madhab = Madhab.HANAFI, var highLatFajr: HighLatFajr = HighLatFajr.AQRABUL_AYYAM, var highLatIsha: HighLatIsha = HighLatIsha.HARAJ )