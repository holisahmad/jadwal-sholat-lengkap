package com.jadwalsholat.lengkap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.R

// ─── Font Arab: 3 pilihan tersedia ───────────────────────────────────────────

/**
 * Scheherazade New (SIL International) — default.
 * Dirancang khusus untuk Al-Qur'an ber-harakat. Lisensi: OFL 1.1.
 */
val ScheherazadeFamily = FontFamily(
    Font(R.font.scheherazade_new_regular, FontWeight.Normal),
    Font(R.font.scheherazade_new_bold, FontWeight.Bold)
)

/**
 * Amiri Quran (Khaled Hosny) — gaya kaligrafi klasik bergaya Bulaq.
 * Optimal untuk teks Al-Qur'an fully vocalized. Lisensi: OFL 1.1.
 */
val AmiriQuranFamily = FontFamily(
    Font(R.font.amiri_quran_regular, FontWeight.Normal)
)

/**
 * Noto Naskh Arabic (Google/Adobe) — modern & bersih, rendering solid di semua device.
 * Lisensi: OFL 1.1.
 */
val NotoNaskhFamily = FontFamily(
    Font(R.font.noto_naskh_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic_bold, FontWeight.Bold)
)

/** Enum kunci yang disimpan ke SharedPreferences */
enum class ArabicFontOption(val key: String, val displayName: String) {
    SCHEHERAZADE("scheherazade", "Scheherazade New"),
    AMIRI("amiri", "Amiri Quran"),
    NOTO_NASKH("noto_naskh", "Noto Naskh Arabic")
}

/** Resolve string preference → FontFamily aktif */
fun getArabicFontFamily(key: String): FontFamily = when (key) {
    ArabicFontOption.AMIRI.key     -> AmiriQuranFamily
    ArabicFontOption.NOTO_NASKH.key -> NotoNaskhFamily
    else                           -> ScheherazadeFamily   // default
}

/** Default Arabic FontFamily (Scheherazade New) */
val ArabicFontFamily = ScheherazadeFamily

// ─────────────────────────────────────────────────────────────────────────────


val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp
    )
)
