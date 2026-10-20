package com.jadwalsholat.lengkap.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jadwal_sholat_lengkap_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CITY_ID = "pref_city_id"
        private const val KEY_CALC_METHOD = "pref_calc_method"
        private const val KEY_GOLD_PRICE = "pref_gold_price"
        private const val KEY_LAST_SURAH = "pref_last_surah"
        private const val KEY_LAST_AYAH = "pref_last_ayah"
        private const val KEY_LAST_SURAH_NAME = "pref_last_surah_name"
        private const val KEY_BOOKMARKS = "pref_bookmarks"
        private const val KEY_QURAN_ARAB_ONLY = "pref_quran_arab_only"
        private const val KEY_THEME_MODE = "pref_theme_mode" // "system", "dark", "light"
        private const val KEY_ARABIC_FONT = "pref_arabic_font" // "scheherazade", "amiri", "noto_naskh"
        private const val KEY_NOTIF_ENABLED = "pref_notif_enabled"
        private const val KEY_NOTIF_OFFSET = "pref_notif_offset"
        private const val KEY_TASBIH_COUNT = "pref_tasbih_count"
        private const val KEY_TASBIH_TARGET = "pref_tasbih_target"
    }

    var cityId: String
        get() = prefs.getString(KEY_CITY_ID, CityData.DEFAULT_CITY.id) ?: CityData.DEFAULT_CITY.id
        set(value) = prefs.edit().putString(KEY_CITY_ID, value).apply()

    var calculationMethod: String
        get() = prefs.getString(KEY_CALC_METHOD, "singapore") ?: "singapore"
        set(value) = prefs.edit().putString(KEY_CALC_METHOD, value).apply()

    var goldPrice: Double
        get() = prefs.getString(KEY_GOLD_PRICE, "1450000")?.toDoubleOrNull() ?: 1450000.0
        set(value) = prefs.edit().putString(KEY_GOLD_PRICE, value.toString()).apply()

    var lastSurah: Int
        get() = prefs.getInt(KEY_LAST_SURAH, 1)
        set(value) = prefs.edit().putInt(KEY_LAST_SURAH, value).apply()

    var lastAyah: Int
        get() = prefs.getInt(KEY_LAST_AYAH, 1)
        set(value) = prefs.edit().putInt(KEY_LAST_AYAH, value).apply()

    var lastSurahName: String
        get() = prefs.getString(KEY_LAST_SURAH_NAME, "Al-Faatiha") ?: "Al-Faatiha"
        set(value) = prefs.edit().putString(KEY_LAST_SURAH_NAME, value).apply()

    fun getBookmarks(): Set<String> {
        return prefs.getStringSet(KEY_BOOKMARKS, setOf("1", "36", "67", "112")) ?: emptySet()
    }

    /** true = tampilkan ayat Arab saja (mode mengaji), false = tampilkan beserta terjemahan */
    var quranArabOnly: Boolean
        get() = prefs.getBoolean(KEY_QURAN_ARAB_ONLY, false)
        set(value) = prefs.edit().putBoolean(KEY_QURAN_ARAB_ONLY, value).apply()

    /** "system", "dark", "light" */
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "dark") ?: "dark"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    /** "scheherazade", "amiri", "noto_naskh" */
    var arabicFont: String
        get() = prefs.getString(KEY_ARABIC_FONT, "scheherazade") ?: "scheherazade"
        set(value) = prefs.edit().putString(KEY_ARABIC_FONT, value).apply()

    fun toggleBookmark(surahNumber: Int): Boolean {
        val current = getBookmarks().toMutableSet()
        val key = surahNumber.toString()
        val isNowBookmarked = if (current.contains(key)) {
            current.remove(key)
            false
        } else {
            current.add(key)
            true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, current).apply()
        return isNowBookmarked
    }

    fun isBookmarked(surahNumber: Int): Boolean {
        return getBookmarks().contains(surahNumber.toString())
    }

    fun getTodayKey(date: Date = Date()): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    fun isPrayerChecked(prayerType: PrayerType, date: Date = Date()): Boolean {
        val key = "prayer_${getTodayKey(date)}_${prayerType.name}"
        return prefs.getBoolean(key, false)
    }

    fun togglePrayerCheck(prayerType: PrayerType, date: Date = Date()): Boolean {
        val key = "prayer_${getTodayKey(date)}_${prayerType.name}"
        val current = prefs.getBoolean(key, false)
        val updated = !current
        prefs.edit().putBoolean(key, updated).apply()
        return updated
    }

    fun getPrayerStreak(): Int {
        val trackable = listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)
        val cal = Calendar.getInstance()
        var streak = 0

        for (i in 0 until 60) {
            val keyDate = getTodayKey(cal.time)
            val allChecked = trackable.all { prayer ->
                prefs.getBoolean("prayer_${keyDate}_${prayer.name}", false)
            }

            if (!allChecked) {
                if (i == 0) {
                    // Today might not be finished yet, check yesterday
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    continue
                }
                break
            }

            streak++
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        return streak
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    var notifEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_ENABLED, value).apply()

    var notifOffsetMinutes: Int
        get() = prefs.getInt(KEY_NOTIF_OFFSET, 0)
        set(value) = prefs.edit().putInt(KEY_NOTIF_OFFSET, value).apply()

    fun isPrayerNotifEnabled(prayerType: PrayerType): Boolean {
        return prefs.getBoolean("notif_${prayerType.name}", true)
    }

    fun setPrayerNotifEnabled(prayerType: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean("notif_${prayerType.name}", enabled).apply()
    }

    // ==========================================
    // EXTRA SUNNAH / IBADAH TRACKING
    // ==========================================
    fun isExtraIbadahChecked(key: String, date: Date = Date()): Boolean {
        val prefKey = "extra_${getTodayKey(date)}_$key"
        return prefs.getBoolean(prefKey, false)
    }

    fun toggleExtraIbadah(key: String, date: Date = Date()): Boolean {
        val prefKey = "extra_${getTodayKey(date)}_$key"
        val current = prefs.getBoolean(prefKey, false)
        val updated = !current
        prefs.edit().putBoolean(prefKey, updated).apply()
        return updated
    }

    // ==========================================
    // 7-DAY PROGRESS HISTORY
    // ==========================================
    data class DayProgressItem(
        val dateKey: String,
        val dayName: String,
        val completedCount: Int,
        val isToday: Boolean
    )

    fun getPast7DaysProgress(date: Date = Date()): List<DayProgressItem> {
        val trackable = listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)
        val dayNames = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        val items = mutableListOf<DayProgressItem>()
        val todayStr = getTodayKey(date)

        val cal = Calendar.getInstance()
        cal.time = date
        // Go 6 days back to show chronological order (6 days ago -> today)
        cal.add(Calendar.DAY_OF_MONTH, -6)

        for (i in 0..6) {
            val key = getTodayKey(cal.time)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            val dayName = dayNames[dayOfWeek.coerceIn(0, 6)]
            val completed = trackable.count { prayer ->
                prefs.getBoolean("prayer_${key}_${prayer.name}", false)
            }
            items.add(
                DayProgressItem(
                    dateKey = key,
                    dayName = dayName,
                    completedCount = completed,
                    isToday = key == todayStr
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return items
    }

    // ==========================================
    // DIGITAL TASBIH
    // ==========================================
    var tasbihCount: Int
        get() = prefs.getInt(KEY_TASBIH_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_TASBIH_COUNT, value).apply()

    var tasbihTarget: Int
        get() = prefs.getInt(KEY_TASBIH_TARGET, 33)
        set(value) = prefs.edit().putInt(KEY_TASBIH_TARGET, value).apply()
}
