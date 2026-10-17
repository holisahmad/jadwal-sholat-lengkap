package com.jadwalsholat.lengkap.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.City
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
import com.jadwalsholat.lengkap.ui.theme.NurPrimaryFg
import com.jadwalsholat.lengkap.ui.theme.NurRaised
import com.jadwalsholat.lengkap.ui.theme.NurSurface
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun SholatScreen(
    city: City,
    preferenceManager: PreferenceManager,
    onCityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onViewMonthlyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentDate by remember { mutableStateOf(Date()) }
    var schedule by remember(city, preferenceManager.calculationMethod, currentDate) {
        mutableStateOf(PrayerCalculator.calculate(city, currentDate, preferenceManager.calculationMethod))
    }
    // Incremented each time a prayer is toggled to force recompose of isChecked values
    var checkRevision by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentDate = Date()
        }
    }

    // Compass Sensor logic
    var deviceAzimuth by remember { mutableFloatStateOf(0f) }
    val qiblaDegree = remember(city) { PrayerCalculator.getQiblaDegree(city).toFloat() }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                    hasGravity = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                    hasGeomagnetic = true
                }

                if (hasGravity && hasGeomagnetic) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azimuthRad = orientation[0]
                        var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                        if (azimuthDeg < 0) azimuthDeg += 360f
                        deviceAzimuth = (deviceAzimuth * 0.85f + azimuthDeg * 0.15f)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose { sensorManager.unregisterListener(listener) }
    }

    val animatedDialRotation by animateFloatAsState(targetValue = -deviceAzimuth, label = "dial")
    val needleAngle = (qiblaDegree - deviceAzimuth + 360f) % 360f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NurBg)
    ) {
        NurHeader(
            tag = "WAKTU",
            title = "Sholat",
            subtitle = "${HijriDateHelper.getGregorianDate(currentDate, city.tz)} · ${HijriDateHelper.getHijriDate(currentDate, city.tz)}",
            cityName = city.name,
            onCityClick = onCityClick,
            onSettingsClick = onSettingsClick
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Prayer Times Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        schedule.items.forEachIndexed { index, prayerItem ->
                            // checkRevision is read here so Compose tracks it and recomposes on change
                            @Suppress("UNUSED_EXPRESSION") checkRevision
                            val isChecked = preferenceManager.isPrayerChecked(prayerItem.type, currentDate)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = prayerItem.type.label,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NurFg
                                    )
                                    Text(
                                        text = if (prayerItem.type.isTrackable) "Wajib" else "Penanda",
                                        fontSize = 11.sp,
                                        color = NurMuted
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prayerItem.formattedTime,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NurFg
                                    )

                                    if (prayerItem.type.isTrackable) {
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isChecked) NurPrimary else NurRaised)
                                                .clickable {
                                                    preferenceManager.togglePrayerCheck(prayerItem.type, currentDate)
                                                    checkRevision++  // triggers recompose
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (isChecked) "Selesai" else "Tandai",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isChecked) NurPrimaryFg else NurMuted
                                            )
                                        }
                                    }
                                }
                            }

                            if (index < schedule.items.size - 1) {
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Monthly Timetable Action
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, NurBorder, RoundedCornerShape(12.dp))
                        .background(NurRaised)
                        .clickable { onViewMonthlyClick() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
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
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Lihat Jadwal Sholat 1 Bulan Penuh",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = NurFg
                            )
                        }
                        Text(text = "Buka ›", fontSize = 12.sp, color = NurPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Embedded Qibla Compass (Matching Web Style)
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ARAH KIBLAT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = NurMuted
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val dialBgColor = NurRaised
                            val dialBorderColor = NurBorder
                            val dialPrimaryColor = NurPrimary
                            val dialFgColor = NurFg

                            // Dial Canvas
                            Canvas(
                                modifier = Modifier
                                    .size(200.dp)
                                    .rotate(animatedDialRotation)
                            ) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.width / 2 - 4.dp.toPx()

                                drawCircle(
                                    color = dialBgColor,
                                    radius = radius
                                )
                                drawCircle(
                                    color = dialBorderColor,
                                    radius = radius,
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                drawCircle(
                                    color = dialBorderColor.copy(alpha = 0.5f),
                                    radius = radius - 20.dp.toPx(),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Qibla target mark on dial
                                val qiblaRad = Math.toRadians(qiblaDegree.toDouble())
                                val qX = center.x + (radius - 12.dp.toPx()) * Math.sin(qiblaRad).toFloat()
                                val qY = center.y - (radius - 12.dp.toPx()) * Math.cos(qiblaRad).toFloat()
                                drawCircle(
                                    color = dialPrimaryColor,
                                    radius = 6.dp.toPx(),
                                    center = Offset(qX, qY)
                                )
                            }

                            // Pointer Needle
                            Canvas(modifier = Modifier.size(140.dp)) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val qAngle = Math.toRadians(needleAngle.toDouble())
                                val endX = center.x + 50.dp.toPx() * Math.sin(qAngle).toFloat()
                                val endY = center.y - 50.dp.toPx() * Math.cos(qAngle).toFloat()

                                drawLine(
                                    color = dialPrimaryColor,
                                    start = center,
                                    end = Offset(endX, endY),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                drawCircle(
                                    color = dialFgColor,
                                    radius = 5.dp.toPx(),
                                    center = center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${qiblaDegree.roundToInt()}° dari utara · heading ${deviceAzimuth.roundToInt()}°",
                            fontSize = 13.sp,
                            color = NurMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        AdmobBanner(modifier = Modifier.background(NurSurface))
    }
}
