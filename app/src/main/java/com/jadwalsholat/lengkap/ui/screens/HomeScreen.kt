package com.jadwalsholat.lengkap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.City
import com.jadwalsholat.lengkap.data.FeaturedVersesData
import com.jadwalsholat.lengkap.data.HijriDateHelper
import com.jadwalsholat.lengkap.data.PrayerCalculator
import com.jadwalsholat.lengkap.data.PrayerType
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.ui.components.NurHeader
import com.jadwalsholat.lengkap.ui.theme.NurBg
import com.jadwalsholat.lengkap.ui.theme.NurBorder
import com.jadwalsholat.lengkap.ui.theme.NurFaint
import com.jadwalsholat.lengkap.ui.theme.NurFg
import com.jadwalsholat.lengkap.ui.theme.NurMuted
import com.jadwalsholat.lengkap.ui.theme.NurPrimary
import com.jadwalsholat.lengkap.ui.theme.NurRaised
import com.jadwalsholat.lengkap.ui.theme.NurSurface
import com.jadwalsholat.lengkap.ui.theme.getArabicFontFamily
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun HomeScreen(
    selectedCity: City,
    preferenceManager: PreferenceManager,
    onCityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToSholat: () -> Unit,
    onNavigateToDoa: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDate by remember { mutableStateOf(Date()) }
    var schedule by remember(selectedCity, preferenceManager.calculationMethod, currentDate) {
        mutableStateOf(
            PrayerCalculator.calculate(
                selectedCity,
                currentDate,
                preferenceManager.calculationMethod
            )
        )
    }

    var streak by remember { mutableStateOf(preferenceManager.getPrayerStreak()) }
    val featuredVerse = remember { FeaturedVersesData.getDailyVerse() }
    val arabicFont = remember { getArabicFontFamily(preferenceManager.arabicFont) }

    // Real-time 1 second countdown updater
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentDate = Date()
            schedule = PrayerCalculator.calculate(
                selectedCity,
                currentDate,
                preferenceManager.calculationMethod
            )
        }
    }

    val hijriDate = remember(currentDate, selectedCity.tz) {
        HijriDateHelper.getHijriDate(currentDate, selectedCity.tz)
    }
    val gregorianDate = remember(currentDate, selectedCity.tz) {
        HijriDateHelper.getGregorianDate(currentDate, selectedCity.tz)
    }

    val trackablePrayers = listOf(
        PrayerType.FAJR,
        PrayerType.DHUHR,
        PrayerType.ASR,
        PrayerType.MAGHRIB,
        PrayerType.ISHA
    )

    val completedCount = trackablePrayers.count {
        preferenceManager.isPrayerChecked(it, currentDate)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NurBg)
    ) {
        NurHeader(
            tag = "NUR",
            title = "Pendamping ibadah",
            subtitle = "$gregorianDate · $hijriDate",
            cityName = selectedCity.name,
            onCityClick = onCityClick,
            onSettingsClick = onSettingsClick
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // 1. Hero Next Prayer Card (Matching Web exactly)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "BERIKUTNYA · ${selectedCity.name.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color = NurMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = schedule.nextPrayer?.type?.label ?: "Subuh",
                            fontFamily = FontFamily.Serif,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = NurFg,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = schedule.nextPrayer?.formattedTime ?: "--:--",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NurPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tersisa -${schedule.countdownFormatted}",
                            fontSize = 14.sp,
                            color = NurMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Sholat Hari Ini (5 compact horizontal pill buttons)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Sholat hari ini",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NurFg
                    )

                    Text(
                        text = "$completedCount/${trackablePrayers.size}${if (streak > 0) " · streak $streak hari" else ""}",
                        fontSize = 12.sp,
                        color = NurMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trackablePrayers.forEach { prayer ->
                        val item = schedule.items.find { it.type == prayer }
                        val isDone = preferenceManager.isPrayerChecked(prayer, currentDate)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isDone) NurPrimary else NurBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(if (isDone) NurRaised else NurSurface)
                                .clickable {
                                    preferenceManager.togglePrayerCheck(prayer, currentDate)
                                    streak = preferenceManager.getPrayerStreak()
                                    schedule = PrayerCalculator.calculate(
                                        selectedCity,
                                        currentDate,
                                        preferenceManager.calculationMethod
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selesai",
                                        tint = NurPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = item?.formattedTime ?: "--:--",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = NurMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prayer.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDone) NurPrimary else NurFg
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. Ayat Hari Ini (Featured Daily Verse)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "AYAT HARI INI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color = NurMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = featuredVerse.arab,
                            fontSize = 20.sp,
                            lineHeight = 42.sp,
                            fontFamily = arabicFont,
                            textAlign = TextAlign.End,
                            color = NurFg,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = featuredVerse.id,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = NurMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "QS. ${featuredVerse.name} ${featuredVerse.surah}:${featuredVerse.ayah}",
                            fontSize = 11.sp,
                            color = NurFaint
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. Lanjutkan Baca Al-Qur'an Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToQuran() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NurRaised),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = NurPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Lanjutkan baca",
                                    fontSize = 11.sp,
                                    color = NurMuted
                                )
                                Text(
                                    text = "${preferenceManager.lastSurahName} · ayat ${preferenceManager.lastAyah}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NurFg
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = NurFaint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Quick Links (Kiblat & Jadwal, Kumpulan Doa)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NurBorder, RoundedCornerShape(12.dp))
                            .background(NurRaised)
                            .clickable { onNavigateToSholat() }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Kiblat & jadwal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NurFg
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NurBorder, RoundedCornerShape(12.dp))
                            .background(NurRaised)
                            .clickable { onNavigateToDoa() }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Kumpulan doa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NurFg
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    text = "Waktu sholat dihitung di perangkat. Bukan pengganti keputusan ulama setempat.",
                    fontSize = 11.sp,
                    color = NurFaint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }
        }

        AdmobBanner(modifier = Modifier.background(NurSurface))
    }
}
