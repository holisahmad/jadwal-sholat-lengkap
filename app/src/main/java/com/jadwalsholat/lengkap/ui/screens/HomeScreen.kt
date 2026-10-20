package com.jadwalsholat.lengkap.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jadwalsholat.lengkap.data.City
import com.jadwalsholat.lengkap.data.FeaturedVersesData
import com.jadwalsholat.lengkap.data.HijriDateHelper
import com.jadwalsholat.lengkap.data.PrayerCalculator
import com.jadwalsholat.lengkap.data.PrayerType
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.notification.PrayerNotificationHelper
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
    val context = LocalContext.current
    val view = LocalView.current

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
    var past7Days by remember(currentDate) { mutableStateOf(preferenceManager.getPast7DaysProgress(currentDate)) }
    var isNotifActive by remember { mutableStateOf(preferenceManager.notifEnabled) }

    // Tasbih state
    var tasbihCount by remember { mutableIntStateOf(preferenceManager.tasbihCount) }
    var tasbihTarget by remember { mutableIntStateOf(preferenceManager.tasbihTarget) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    val featuredVerse = remember { FeaturedVersesData.getDailyVerse() }
    val arabicFont = remember { getArabicFontFamily(preferenceManager.arabicFont) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isNotifActive = true
            preferenceManager.notifEnabled = true
            PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, selectedCity)
            PrayerNotificationHelper.showTestNotification(context)
            Toast.makeText(context, "Notifikasi waktu sholat aktif", Toast.LENGTH_SHORT).show()
        }
    }

    val tasbihPresets = listOf(
        Triple("Subhanallah", "سُبْحَانَ اللَّهِ", 33),
        Triple("Alhamdulillah", "الْحَمْدُ لِلَّهِ", 33),
        Triple("Allahu Akbar", "اللَّهُ أَكْبَرُ", 33),
        Triple("Astaghfirullah", "أَسْتَغْفِرُ اللَّهَ", 100)
    )

    val extraIbadahList = listOf(
        Triple("dzikirPagi", "Dzikir Pagi", "🌅"),
        Triple("dzikirPetang", "Dzikir Petang", "🌇"),
        Triple("dhuha", "Sholat Dhuha", "☀️"),
        Triple("tahajjud", "Sholat Tahajjud", "🌙"),
        Triple("rawatib", "Sholat Rawatib", "🕌"),
        Triple("bacaQuran", "Tilawah Quran", "📖")
    )

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
            // 0. Quick Notification Banner (if disabled)
            if (!isNotifActive) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NurPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NurPrimary.copy(alpha = 0.12f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NurPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = NurPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Pengingat Waktu Sholat",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NurFg
                                    )
                                    Text(
                                        text = "Alert suara & getar saat adzan",
                                        fontSize = 10.sp,
                                        color = NurMuted
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        isNotifActive = true
                                        preferenceManager.notifEnabled = true
                                        PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, selectedCity)
                                        PrayerNotificationHelper.showTestNotification(context)
                                        Toast.makeText(context, "Notifikasi waktu sholat aktif", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NurPrimary)
                            ) {
                                Text(text = "Aktifkan", fontSize = 11.sp, color = NurBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

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
                                    past7Days = preferenceManager.getPast7DaysProgress(currentDate)
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

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. RIWAYAT 7 HARI TERAKHIR (BARU)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = NurPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Riwayat 7 Hari Terakhir",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NurFg
                                )
                            }
                            Text(
                                text = "Konsistensi Sholat",
                                fontSize = 11.sp,
                                color = NurMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            past7Days.forEach { dayItem ->
                                val fraction = dayItem.completedCount / 5f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (dayItem.isToday) NurPrimary.copy(alpha = 0.6f) else NurBorder.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(if (dayItem.isToday) NurPrimary.copy(alpha = 0.1f) else NurRaised.copy(alpha = 0.4f))
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayItem.dayName,
                                            fontSize = 10.sp,
                                            color = NurMuted,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${dayItem.completedCount}/5",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dayItem.completedCount == 5) NurPrimary else if (dayItem.completedCount > 0) NurFg else NurFaint
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { fraction },
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = NurPrimary,
                                            trackColor = NurBorder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. AMALAN & DZIKIR HARIAN SUNNAH (BARU)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val activeExtraCount = extraIbadahList.count { (key, _, _) ->
                            preferenceManager.isExtraIbadahChecked(key, currentDate)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Amalan & Dzikir Harian",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NurFg
                                )
                                Text(
                                    text = "Sunnah penambah pahala",
                                    fontSize = 11.sp,
                                    color = NurMuted
                                )
                            }
                            Text(
                                text = "$activeExtraCount/${extraIbadahList.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NurPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid 2 columns
                        val chunked = extraIbadahList.chunked(2)
                        chunked.forEachIndexed { idx, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { (key, label, emoji) ->
                                    var checked by remember {
                                        mutableStateOf(preferenceManager.isExtraIbadahChecked(key, currentDate))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = 1.dp,
                                                color = if (checked) NurPrimary.copy(alpha = 0.6f) else NurBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .background(if (checked) NurPrimary.copy(alpha = 0.12f) else NurRaised.copy(alpha = 0.4f))
                                            .clickable {
                                                checked = preferenceManager.toggleExtraIbadah(key, currentDate)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(text = emoji, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (checked) NurFg else NurMuted,
                                                    maxLines = 1
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .border(1.dp, if (checked) NurPrimary else NurBorder, RoundedCornerShape(4.dp))
                                                    .background(if (checked) NurPrimary else Color.Transparent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (checked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = NurBg,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (idx < chunked.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. TASBIH DIGITAL MINI WIDGET (BARU)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = NurPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tasbih Digital",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NurFg
                                )
                            }

                            IconButton(
                                onClick = {
                                    tasbihCount = 0
                                    preferenceManager.tasbihCount = 0
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset",
                                    tint = NurMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tasbihPresets.forEachIndexed { index, (label, _, target) ->
                                val isSelected = selectedPresetIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) NurPrimary else NurBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(if (isSelected) NurPrimary.copy(alpha = 0.15f) else NurRaised.copy(alpha = 0.3f))
                                        .clickable {
                                            selectedPresetIndex = index
                                            tasbihTarget = target
                                            preferenceManager.tasbihTarget = target
                                            tasbihCount = 0
                                            preferenceManager.tasbihCount = 0
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        color = if (isSelected) NurPrimary else NurMuted,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tap container
                        val curPreset = tasbihPresets[selectedPresetIndex]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, NurBorder.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                                .background(NurRaised.copy(alpha = 0.6f))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    val nextVal = tasbihCount + 1
                                    tasbihCount = nextVal
                                    preferenceManager.tasbihCount = nextVal
                                    if (nextVal >= tasbihTarget) {
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    }
                                }
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = curPreset.second,
                                    fontFamily = arabicFont,
                                    fontSize = 22.sp,
                                    color = NurFg.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$tasbihCount",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = NurPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Target: $tasbihTarget · Ketuk untuk berdzikir",
                                    fontSize = 11.sp,
                                    color = NurMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                val progressFraction = (tasbihCount.toFloat() / tasbihTarget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = NurPrimary,
                                    trackColor = NurBorder
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. Ayat Hari Ini (Featured Daily Verse)
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

            // 7. Lanjutkan Baca Al-Qur'an Card
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

            // 8. Quick Links (Kiblat & Jadwal, Kumpulan Doa)
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
                            text = "🧭 Kiblat & jadwal",
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
                            text = "🤲 Kumpulan doa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NurFg
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
