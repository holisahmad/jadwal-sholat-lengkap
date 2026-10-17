package com.jadwalsholat.lengkap.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.City
import com.jadwalsholat.lengkap.data.HijriDateHelper
import com.jadwalsholat.lengkap.data.PrayerCalculator
import com.jadwalsholat.lengkap.ui.components.NurHeader
import com.jadwalsholat.lengkap.ui.theme.EmeraldPrimary
import com.jadwalsholat.lengkap.ui.theme.GoldAccent
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(
    city: City,
    onCityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var deviceAzimuth by remember { mutableFloatStateOf(0f) }
    val qiblaDegree = remember(city) {
        PrayerCalculator.getQiblaDegree(city).toFloat()
    }

    // Register sensor listener
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
                        // Simple low pass filter
                        deviceAzimuth = (deviceAzimuth * 0.8f + azimuthDeg * 0.2f)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val animatedDialRotation by animateFloatAsState(
        targetValue = -deviceAzimuth,
        label = "compassDial"
    )

    val relativeQiblaAngle = (qiblaDegree - deviceAzimuth + 360f) % 360f
    val isFacingQibla = relativeQiblaAngle in 355f..360f || relativeQiblaAngle in 0f..5f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NurHeader(
            tag = "KIBLAT",
            title = "Arah Kiblat",
            subtitle = HijriDateHelper.getHijriDate(Date(), city.tz),
            cityName = city.name,
            onCityClick = onCityClick,
            onSettingsClick = onSettingsClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Qibla degree card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFacingQibla) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Derajat Kiblat Ka'bah",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${qiblaDegree.roundToInt()}° dari Utara",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isFacingQibla) EmeraldPrimary else GoldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = if (isFacingQibla) Color.White else EmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Compass Visual Graphic
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rotating dial
                Canvas(
                    modifier = Modifier
                        .size(280.dp)
                        .rotate(animatedDialRotation)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2 - 8.dp.toPx()

                    // Outer dial circle
                    drawCircle(
                        color = Color(0xFFE2E8F0),
                        radius = radius,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Inner circle
                    drawCircle(
                        color = Color(0xFF0F3D3E).copy(alpha = 0.05f),
                        radius = radius - 6.dp.toPx()
                    )

                    // Tick marks & N S E W markers
                    for (i in 0 until 360 step 30) {
                        val angleRad = Math.toRadians(i.toDouble())
                        val tickLen = if (i % 90 == 0) 16.dp.toPx() else 8.dp.toPx()
                        val startX = center.x + (radius - tickLen) * Math.sin(angleRad).toFloat()
                        val startY = center.y - (radius - tickLen) * Math.cos(angleRad).toFloat()
                        val endX = center.x + radius * Math.sin(angleRad).toFloat()
                        val endY = center.y - radius * Math.cos(angleRad).toFloat()

                        drawLine(
                            color = if (i == 0) Color(0xFFDC2626) else Color(0xFF64748B),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                        )
                    }

                    // Qibla needle indicator on the dial
                    val qiblaAngleRad = Math.toRadians(qiblaDegree.toDouble())
                    val qiblaX = center.x + (radius - 28.dp.toPx()) * Math.sin(qiblaAngleRad).toFloat()
                    val qiblaY = center.y - (radius - 28.dp.toPx()) * Math.cos(qiblaAngleRad).toFloat()

                    drawCircle(
                        color = Color(0xFFD4AF37),
                        radius = 12.dp.toPx(),
                        center = Offset(qiblaX, qiblaY)
                    )
                }

                // Stationary Device Target Needle (pointing up)
                Canvas(modifier = Modifier.size(160.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val needlePath = Path().apply {
                        moveTo(center.x, center.y - 70.dp.toPx())
                        lineTo(center.x + 10.dp.toPx(), center.y)
                        lineTo(center.x - 10.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = needlePath,
                        brush = Brush.verticalGradient(
                            colors = if (isFacingQibla) listOf(Color(0xFF10B981), Color(0xFF059669))
                            else listOf(Color(0xFFD4AF37), Color(0xFF0F3D3E))
                        )
                    )

                    drawCircle(
                        color = Color(0xFF0F3D3E),
                        radius = 14.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFFD4AF37),
                        radius = 6.dp.toPx(),
                        center = center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isFacingQibla) "✓ Tepat Menghadap Kiblat!" else "Putar perangkat hingga panah sejajar ikon Ka'bah",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isFacingQibla) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Kompas membutuhkan sensor magnetik. Jauhkan dari benda logam untuk hasil akurat.",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        AdmobBanner(modifier = Modifier.background(MaterialTheme.colorScheme.surface))
    }
}
