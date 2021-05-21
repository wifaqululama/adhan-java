package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Madhab

object WifaqDefaultCalculationParams {

    fun getCalculationParams(): CalculationParameters{
        val parameters = CalculationMethod.KARACHI.parameters
        parameters.ishaAngle = 15.0
        parameters.madhab = Madhab.HANAFI
        parameters.adjustments.maghrib = 5
        parameters.adjustments.dhuhr = 4
        return parameters
    }
}