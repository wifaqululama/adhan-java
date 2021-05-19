package uk.co.wifaqululama.adhan

import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Madhab

object WifaqDefaultCalculationParams {

    fun getCalculationParams(): CalculationParameters{
        val parameters = CalculationParameters(18.0,18.0)
        parameters.madhab = Madhab.HANAFI
        parameters.adjustments.maghrib = 5
        parameters.adjustments.dhuhr = 5
        return parameters
    }
}