package com.jadwalsholat.lengkap.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jadwalsholat.lengkap.MainActivity
import com.jadwalsholat.lengkap.R
import com.jadwalsholat.lengkap.data.City
import com.jadwalsholat.lengkap.data.CityData
import com.jadwalsholat.lengkap.data.PrayerCalculator
import com.jadwalsholat.lengkap.data.PrayerType
import com.jadwalsholat.lengkap.data.PreferenceManager
import java.util.Calendar
import java.util.Date

object PrayerNotificationHelper {
    const val CHANNEL_ID = "jadwal_sholat_adhan_channel"
    const val CHANNEL_NAME = "Pengingat Waktu Sholat"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_TIME = "extra_prayer_time"
    const val ACTION_PRAYER_ALARM = "com.jadwalsholat.lengkap.ACTION_PRAYER_ALARM"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat waktu sholat 5 waktu otomatis"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(defaultSoundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAlarms(context: Context, preferenceManager: PreferenceManager, city: City) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!preferenceManager.notifEnabled) {
            cancelAllAlarms(context)
            return
        }

        createNotificationChannel(context)

        val trackable = listOf(
            PrayerType.FAJR,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )

        val offsetMs = preferenceManager.notifOffsetMinutes * 60_000L
        val now = System.currentTimeMillis()

        // Schedule for today and tomorrow
        val daysToSchedule = listOf(0, 1)

        for (dayOffset in daysToSchedule) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, dayOffset)
            }
            val schedule = PrayerCalculator.calculate(
                city,
                cal.time,
                preferenceManager.calculationMethod
            )

            trackable.forEachIndexed { prayerIdx, prayerType ->
                if (!preferenceManager.isPrayerNotifEnabled(prayerType)) return@forEachIndexed

                val prayerItem = schedule.items.find { it.type == prayerType } ?: return@forEachIndexed
                val triggerTime = prayerItem.timeMillis - offsetMs

                if (triggerTime > now) {
                    val requestCode = (dayOffset * 100) + prayerIdx

                    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                        action = ACTION_PRAYER_ALARM
                        putExtra(EXTRA_PRAYER_NAME, prayerType.label)
                        putExtra(EXTRA_PRAYER_TIME, prayerItem.formattedTime)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        } else {
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        }
                    } catch (_: SecurityException) {
                        // In case exact alarm permission is restricted on Android 14+
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // 5 prayers * 2 days
        for (dayOffset in 0..1) {
            for (prayerIdx in 0..4) {
                val requestCode = (dayOffset * 100) + prayerIdx
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = ACTION_PRAYER_ALARM
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }

    fun showPrayerNotification(context: Context, prayerName: String, timeStr: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Waktu Sholat $prayerName — $timeStr")
            .setContentText("Assalamualaikum. Telah masuk waktu sholat $prayerName untuk wilayah Anda.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Assalamualaikum. Telah masuk waktu sholat $prayerName ($timeStr). Mari bersiap menunaikan ibadah sholat tepat waktu.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(prayerName.hashCode(), notification)
    }

    fun showTestNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔔 Notifikasi Sholat Aktif")
            .setContentText("Alhamdulillah! Pengingat waktu sholat siap mengingatkan Anda tepat waktu.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }
}
