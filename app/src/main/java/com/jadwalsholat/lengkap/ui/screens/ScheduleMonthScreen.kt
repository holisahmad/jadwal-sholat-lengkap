package com.jadwalsholat.lengkap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.City
import com.jadwalsholat.lengkap.data.PrayerCalculator
import com.jadwalsholat.lengkap.data.PrayerType
import com.jadwalsholat.lengkap.ui.theme.NurBg
import com.jadwalsholat.lengkap.ui.theme.NurBorder
import com.jadwalsholat.lengkap.ui.theme.NurFg
import com.jadwalsholat.lengkap.ui.theme.NurMuted
import com.jadwalsholat.lengkap.ui.theme.NurPrimary
import com.jadwalsholat.lengkap.ui.theme.NurRaised
import com.jadwalsholat.lengkap.ui.theme.NurSurface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMonthScreen(
    city: City,
    calculationMethod: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val currentMonthName = remember {
        SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
    }

    val monthlyList = remember(city, calculationMethod) {
        PrayerCalculator.getMonthlySchedule(
            city = city,
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH),
            methodId = calculationMethod
        )
    }

    val todayDay = cal.get(Calendar.DAY_OF_MONTH)

    Scaffold(
        containerColor = NurBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Jadwal Sholat Bulanan",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NurFg
                        )
                        Text(
                            text = "${city.name} · $currentMonthName",
                            fontSize = 11.sp,
                            color = NurMuted
                        )
                    }
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NurRaised)
                    .border(1.dp, NurBorder)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tgl", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = NurMuted, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text("Subuh", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Dzuhur", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Ashar", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Maghrib", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Isya", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(monthlyList) { index, daySchedule ->
                    val dayNumber = index + 1
                    val isToday = dayNumber == todayDay

                    val subuh = daySchedule.items.find { it.type == PrayerType.FAJR }?.formattedTime ?: "-"
                    val dzuhur = daySchedule.items.find { it.type == PrayerType.DHUHR }?.formattedTime ?: "-"
                    val ashar = daySchedule.items.find { it.type == PrayerType.ASR }?.formattedTime ?: "-"
                    val maghrib = daySchedule.items.find { it.type == PrayerType.MAGHRIB }?.formattedTime ?: "-"
                    val isya = daySchedule.items.find { it.type == PrayerType.ISHA }?.formattedTime ?: "-"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isToday) NurRaised else Color.Transparent
                            )
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayNumber.toString(),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isToday) NurPrimary else NurFg,
                            modifier = Modifier.width(26.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(subuh, fontSize = 12.sp, color = if (isToday) NurPrimary else NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(dzuhur, fontSize = 12.sp, color = if (isToday) NurPrimary else NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(ashar, fontSize = 12.sp, color = if (isToday) NurPrimary else NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(maghrib, fontSize = 12.sp, color = if (isToday) NurPrimary else NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(isya, fontSize = 12.sp, color = if (isToday) NurPrimary else NurMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NurBorder.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}
