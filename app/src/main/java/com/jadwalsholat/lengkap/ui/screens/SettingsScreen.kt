package com.jadwalsholat.lengkap.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.City
import com.jadwalsholat.lengkap.data.PrayerType
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.notification.PrayerNotificationHelper
import com.jadwalsholat.lengkap.ui.theme.NurBg
import com.jadwalsholat.lengkap.ui.theme.NurBorder
import com.jadwalsholat.lengkap.ui.theme.NurFaint
import com.jadwalsholat.lengkap.ui.theme.NurFg
import com.jadwalsholat.lengkap.ui.theme.NurMuted
import com.jadwalsholat.lengkap.ui.theme.NurPrimary
import com.jadwalsholat.lengkap.ui.theme.NurRaised
import com.jadwalsholat.lengkap.ui.theme.NurSurface
import com.jadwalsholat.lengkap.ui.theme.getArabicFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentCity: City,
    preferenceManager: PreferenceManager,
    onPickCityClick: () -> Unit,
    onThemeChanged: (String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf(preferenceManager.calculationMethod) }
    var selectedTheme by remember { mutableStateOf(preferenceManager.themeMode) }
    var selectedArabicFont by remember { mutableStateOf(preferenceManager.arabicFont) }

    var isNotifEnabled by remember { mutableStateOf(preferenceManager.notifEnabled) }
    var notifOffset by remember { mutableIntStateOf(preferenceManager.notifOffsetMinutes) }

    val trackablePrayers = listOf(
        PrayerType.FAJR,
        PrayerType.DHUHR,
        PrayerType.ASR,
        PrayerType.MAGHRIB,
        PrayerType.ISHA
    )

    // Permission launcher for Android 13+ (POST_NOTIFICATIONS)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isNotifEnabled = true
            preferenceManager.notifEnabled = true
            PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, currentCity)
            Toast.makeText(context, "Notifikasi waktu sholat diaktifkan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    val themes = listOf(
        Pair("dark", "Mode Gelap (Nocturnal)"),
        Pair("light", "Mode Terang (Warm Mushaf)"),
        Pair("system", "Ikuti Pengaturan Sistem")
    )

    val methods = listOf(
        Pair("singapore", "Kemenag RI / Singapore (MUIS)"),
        Pair("mwl", "Muslim World League (MWL)"),
        Pair("egypt", "Egyptian General Authority of Survey")
    )

    val offsets = listOf(
        Pair(0, "Tepat waktu"),
        Pair(5, "5 menit sebelum"),
        Pair(10, "10 menit sebelum"),
        Pair(15, "15 menit sebelum")
    )

    val arabicFonts = listOf(
        Triple("scheherazade", "Scheherazade New", "Standar Mushaf Al-Qur'an (Rekomendasi)"),
        Triple("amiri", "Amiri Quran", "Gaya Klasik Kaligrafi (Percetakan Bulaq)"),
        Triple("noto_naskh", "Noto Naskh Arabic", "Gaya Modern & Rapi (Google Noto)")
    )

    Scaffold(
        containerColor = NurBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan",
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NurFg
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = NurFg
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NurSurface)
            )
        },
        bottomBar = {
            AdmobBanner(modifier = Modifier.background(NurSurface))
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 0. NOTIFIKASI WAKTU SHOLAT (BARU)
            item {
                Text(
                    text = "Notifikasi Waktu Sholat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NurFg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isNotifEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (isNotifEnabled) NurPrimary else NurMuted
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Pengingat Adzan",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NurFg
                                    )
                                    Text(
                                        text = "Alert suara & getar saat masuk waktu",
                                        fontSize = 11.sp,
                                        color = NurMuted
                                    )
                                }
                            }

                            Switch(
                                checked = isNotifEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            isNotifEnabled = true
                                            preferenceManager.notifEnabled = true
                                            PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, currentCity)
                                        }
                                    } else {
                                        isNotifEnabled = false
                                        preferenceManager.notifEnabled = false
                                        PrayerNotificationHelper.cancelAllAlarms(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NurFg,
                                    checkedTrackColor = NurPrimary,
                                    uncheckedThumbColor = NurMuted,
                                    uncheckedTrackColor = NurRaised
                                )
                            )
                        }

                        if (isNotifEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(NurBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Offset selection
                            Text(
                                text = "Waktu Pemberitahuan:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NurFg
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            offsets.forEach { (valMinutes, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            notifOffset = valMinutes
                                            preferenceManager.notifOffsetMinutes = valMinutes
                                            PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, currentCity)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = notifOffset == valMinutes,
                                        onClick = {
                                            notifOffset = valMinutes
                                            preferenceManager.notifOffsetMinutes = valMinutes
                                            PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, currentCity)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = NurPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        color = if (notifOffset == valMinutes) NurFg else NurMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(NurBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Prayers selection
                            Text(
                                text = "Pilih Waktu Sholat:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NurFg
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            trackablePrayers.forEach { prayer ->
                                var checked by remember { mutableStateOf(preferenceManager.isPrayerNotifEnabled(prayer)) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val newVal = !checked
                                            checked = newVal
                                            preferenceManager.setPrayerNotifEnabled(prayer, newVal)
                                            PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, currentCity)
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { newVal ->
                                            checked = newVal
                                            preferenceManager.setPrayerNotifEnabled(prayer, newVal)
                                            PrayerNotificationHelper.scheduleAlarms(context, preferenceManager, currentCity)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = NurPrimary,
                                            checkmarkColor = NurSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = prayer.label,
                                        fontSize = 12.sp,
                                        color = if (checked) NurFg else NurMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    PrayerNotificationHelper.showTestNotification(context)
                                    Toast.makeText(context, "Notifikasi percobaan dikirim", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔔 Uji Coba Notifikasi Sekarang",
                                    fontSize = 12.sp,
                                    color = NurPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // City preference
            item {
                Text(
                    text = "Lokasi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NurFg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(12.dp))
                        .clickable { onPickCityClick() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = NurPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Kota Terpilih",
                                    fontSize = 11.sp,
                                    color = NurMuted
                                )
                                Text(
                                    text = currentCity.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NurFg
                                )
                            }
                        }

                        Text(
                            text = "Ganti ›",
                            color = NurPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Calculation Method
            item {
                Text(
                    text = "Metode Hisab",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NurFg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        methods.forEachIndexed { index, (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMethod = key
                                        preferenceManager.calculationMethod = key
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMethod == key,
                                    onClick = {
                                        selectedMethod = key
                                        preferenceManager.calculationMethod = key
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = NurPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    color = if (selectedMethod == key) NurFg else NurMuted,
                                    fontWeight = if (selectedMethod == key) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                            if (index < methods.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(NurBorder.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Theme preference (Dark, Light, System)
            item {
                Text(
                    text = "Tampilan & Tema",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NurFg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        themes.forEachIndexed { index, (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTheme = key
                                        preferenceManager.themeMode = key
                                        onThemeChanged(key)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTheme == key,
                                    onClick = {
                                        selectedTheme = key
                                        preferenceManager.themeMode = key
                                        onThemeChanged(key)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = NurPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    color = if (selectedTheme == key) NurFg else NurMuted,
                                    fontWeight = if (selectedTheme == key) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                            if (index < themes.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(NurBorder.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Arabic Font preference (Scheherazade, Amiri, Noto Naskh)
            item {
                Text(
                    text = "Gaya Font Arab (Al-Qur'an & Doa)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NurFg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        arabicFonts.forEachIndexed { index, (key, label, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedArabicFont = key
                                        preferenceManager.arabicFont = key
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedArabicFont == key,
                                    onClick = {
                                        selectedArabicFont = key
                                        preferenceManager.arabicFont = key
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = NurPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        color = if (selectedArabicFont == key) NurFg else NurMuted,
                                        fontWeight = if (selectedArabicFont == key) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = NurMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        fontSize = 17.sp,
                                        lineHeight = 28.sp,
                                        fontFamily = getArabicFontFamily(key),
                                        color = if (selectedArabicFont == key) NurPrimary else NurFg
                                    )
                                }
                            }
                            if (index < arabicFonts.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(NurBorder.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // About & Legal
            item {
                Text(
                    text = "Tentang Aplikasi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NurFg,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NurPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Nur · Pendamping Ibadah",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NurFg
                                )
                                Text(
                                    text = "Versi 1.0.0 (API 36)",
                                    fontSize = 12.sp,
                                    color = NurMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(NurBorder.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://numos-beige.vercel.app/policies/jadwal-sholat-lengkap.html")
                                    )
                                    try {
                                        context.startActivity(browserIntent)
                                    } catch (_: Exception) {}
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = NurPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Kebijakan Privasi",
                                    fontSize = 13.sp,
                                    color = NurFg
                                )
                            }
                            Text(text = "Buka ›", color = NurPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
