package com.jadwalsholat.lengkap.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jadwalsholat.lengkap.data.CityData
import com.jadwalsholat.lengkap.data.PreferenceManager

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pref = PreferenceManager(context)
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Device rebooted or app updated, reschedule alarms
            val city = CityData.findCity(pref.cityId)
            PrayerNotificationHelper.scheduleAlarms(context, pref, city)
            return
        }

        if (action == PrayerNotificationHelper.ACTION_PRAYER_ALARM) {
            val prayerName = intent.getStringExtra(PrayerNotificationHelper.EXTRA_PRAYER_NAME) ?: "Sholat"
            val prayerTime = intent.getStringExtra(PrayerNotificationHelper.EXTRA_PRAYER_TIME) ?: ""

            // Show the notification
            PrayerNotificationHelper.showPrayerNotification(context, prayerName, prayerTime)

            // Reschedule subsequent alarms so notifications continue automatically
            val city = CityData.findCity(pref.cityId)
            PrayerNotificationHelper.scheduleAlarms(context, pref, city)
        }
    }
}
