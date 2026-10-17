package com.jadwalsholat.lengkap.data

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale

object HijriDateHelper {

    private val HIJRI_MONTHS = listOf(
        "Muharram",
        "Safar",
        "Rabi'ul Awal",
        "Rabi'ul Akhir",
        "Jumadil Awal",
        "Jumadil Akhir",
        "Rajab",
        "Sya'ban",
        "Ramadhan",
        "Syawal",
        "Dzulqa'dah",
        "Dzulhijjah"
    )

    fun getHijriDate(date: Date = Date(), timeZoneId: String = "Asia/Jakarta"): String {
        return try {
            val localDate = date.toInstant().atZone(ZoneId.of(timeZoneId)).toLocalDate()
            val hijrahDate = HijrahDate.from(localDate)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val monthIndex = hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1
            val year = hijrahDate.get(ChronoField.YEAR_OF_ERA)

            val monthName = if (monthIndex in HIJRI_MONTHS.indices) HIJRI_MONTHS[monthIndex] else "Bulan Hijriah"
            "$day $monthName $year H"
        } catch (e: Exception) {
            // Fallback estimation
            "1447 H"
        }
    }

    fun getGregorianDate(date: Date = Date(), timeZoneId: String = "Asia/Jakarta"): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        sdf.timeZone = java.util.TimeZone.getTimeZone(timeZoneId)
        return sdf.format(date)
    }
}
