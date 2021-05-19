package uk.co.wifaqululama.adhan

/**
 * Calculation rules used to determine Isha time during perpetual twilight
 */
enum class HighLatIsha {
    /**
     * First Seventh of the Night
     */
    HARAJ,

    /**
     * Isha will be calculated using the 15 Degree rule
     */
    AL_AHMAR,

    /**
     * Isha is determined based on white twilight
     */
    AL_ABYADH
}