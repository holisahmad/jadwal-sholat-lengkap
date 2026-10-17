package com.jadwalsholat.lengkap.data

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class PrayerType(val label: String, val isTrackable: Boolean) {
    IMSAK("Imsak", false),
    FAJR("Subuh", true),
    SUNRISE("Terbit", false),
    DHUHR("Dzuhur", true),
    ASR("Ashar", true),
    MAGHRIB("Maghrib", true),
    ISHA("Isya", true)
}

data class SinglePrayerTime(
    val type: PrayerType,
    val timeMillis: Long,
    val formattedTime: String
)

data class DayPrayerSchedule(
    val date: Date,
    val items: List<SinglePrayerTime>,
    val nextPrayer: SinglePrayerTime?,
    val countdownFormatted: String
)

object PrayerCalculator {

    fun calculate(
        city: City,
        date: Date = Date(),
        methodId: String = "singapore"
    ): DayPrayerSchedule {
        val coords = Coordinates(city.lat, city.lng)
        val tz = TimeZone.getTimeZone(city.tz)

        val cal = Calendar.getInstance(tz).apply { time = date }
        val dateComponents = DateComponents(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )

        val params = when (methodId.lowercase()) {
            "mwl" -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            "egypt" -> CalculationMethod.EGYPTIAN.parameters
            else -> CalculationMethod.SINGAPORE.parameters // Standard for Indonesia / Kemenag
        }
        params.madhab = Madhab.SHAFI

        val pt = PrayerTimes(coords, dateComponents, params)

        val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID")).apply {
            timeZone = tz
        }

        // Imsak is approximately 10 minutes before Fajr in Indonesia standard
        val fajrDate = pt.fajr ?: Date()
        val imsakDate = Date(fajrDate.time - (10 * 60 * 1000))
        val sunriseDate = pt.sunrise ?: Date()
        val dhuhrDate = pt.dhuhr ?: Date()
        val asrDate = pt.asr ?: Date()
        val maghribDate = pt.maghrib ?: Date()
        val ishaDate = pt.isha ?: Date()

        val items = listOf(
            SinglePrayerTime(PrayerType.IMSAK, imsakDate.time, timeFormat.format(imsakDate)),
            SinglePrayerTime(PrayerType.FAJR, fajrDate.time, timeFormat.format(fajrDate)),
            SinglePrayerTime(PrayerType.SUNRISE, sunriseDate.time, timeFormat.format(sunriseDate)),
            SinglePrayerTime(PrayerType.DHUHR, dhuhrDate.time, timeFormat.format(dhuhrDate)),
            SinglePrayerTime(PrayerType.ASR, asrDate.time, timeFormat.format(asrDate)),
            SinglePrayerTime(PrayerType.MAGHRIB, maghribDate.time, timeFormat.format(maghribDate)),
            SinglePrayerTime(PrayerType.ISHA, ishaDate.time, timeFormat.format(ishaDate))
        )

        val nowMillis = System.currentTimeMillis()
        var nextItem = items.firstOrNull { it.timeMillis > nowMillis }

        // If all prayers passed today, tomorrow's Fajr is next
        if (nextItem == null) {
            val tomorrowCal = Calendar.getInstance(tz).apply {
                time = date
                add(Calendar.DAY_OF_MONTH, 1)
            }
            val tomorrowComponents = DateComponents(
                tomorrowCal.get(Calendar.YEAR),
                tomorrowCal.get(Calendar.MONTH) + 1,
                tomorrowCal.get(Calendar.DAY_OF_MONTH)
            )
            val tomorrowPt = PrayerTimes(coords, tomorrowComponents, params)
            val tomorrowFajr = tomorrowPt.fajr ?: Date()
            nextItem = SinglePrayerTime(PrayerType.FAJR, tomorrowFajr.time, timeFormat.format(tomorrowFajr))
        }

        val diff = (nextItem.timeMillis - nowMillis).coerceAtLeast(0)
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        val seconds = (diff / 1000) % 60

        val countdownStr = if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

        return DayPrayerSchedule(
            date = date,
            items = items,
            nextPrayer = nextItem,
            countdownFormatted = countdownStr
        )
    }

    fun getQiblaDegree(city: City): Double {
        return Qibla(Coordinates(city.lat, city.lng)).direction
    }

    fun getMonthlySchedule(city: City, year: Int, month: Int, methodId: String): List<DayPrayerSchedule> {
        val tz = TimeZone.getTimeZone(city.tz)
        val cal = Calendar.getInstance(tz).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val list = mutableListOf<DayPrayerSchedule>()

        for (d in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            list.add(calculate(city, cal.time, methodId))
        }
        return list
    }
}
