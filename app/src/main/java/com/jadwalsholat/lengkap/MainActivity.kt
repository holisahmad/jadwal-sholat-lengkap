package com.jadwalsholat.lengkap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jadwalsholat.lengkap.ads.AdManager
import com.jadwalsholat.lengkap.data.CityData
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.ui.components.CitySelectionBottomSheet
import com.jadwalsholat.lengkap.ui.components.MainBottomNav
import com.jadwalsholat.lengkap.ui.components.ScreenTab
import com.jadwalsholat.lengkap.ui.screens.DoaScreen
import com.jadwalsholat.lengkap.ui.screens.HomeScreen
import com.jadwalsholat.lengkap.ui.screens.QuranDetailScreen
import com.jadwalsholat.lengkap.ui.screens.QuranListScreen
import com.jadwalsholat.lengkap.ui.screens.ScheduleMonthScreen
import com.jadwalsholat.lengkap.ui.screens.SettingsScreen
import com.jadwalsholat.lengkap.ui.screens.SholatScreen
import com.jadwalsholat.lengkap.ui.screens.ZakatScreen
import com.jadwalsholat.lengkap.ui.theme.JadwalSholatLengkapTheme
import com.jadwalsholat.lengkap.ui.theme.NurBg

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)

        // Inisialisasi UMP Consent & AdMob
        AdManager.initialize(this)

        // Inisialisasi Notification Channel & Alarm Sholat
        com.jadwalsholat.lengkap.notification.PrayerNotificationHelper.createNotificationChannel(this)
        if (preferenceManager.notifEnabled) {
            val city = CityData.findCity(preferenceManager.cityId)
            com.jadwalsholat.lengkap.notification.PrayerNotificationHelper.scheduleAlarms(this, preferenceManager, city)
        }

        setContent {
            var currentTheme by remember { mutableStateOf(preferenceManager.themeMode) }
            JadwalSholatLengkapTheme(themeMode = currentTheme) {
                MainAppContainer(
                    activity = this,
                    preferenceManager = preferenceManager,
                    onThemeChanged = { currentTheme = it }
                )
            }
        }
    }
}

@Composable
fun MainAppContainer(
    activity: ComponentActivity,
    preferenceManager: PreferenceManager,
    onThemeChanged: (String) -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var currentScreen by remember { mutableStateOf("MAIN") }
    var selectedSurahNumber by remember { mutableIntStateOf(1) }
    var isCityPickerOpen by remember { mutableStateOf(false) }

    var selectedCity by remember {
        mutableStateOf(CityData.findCity(preferenceManager.cityId))
    }

    // Handle back button
    BackHandler(enabled = currentScreen != "MAIN") {
        currentScreen = "MAIN"
    }

    Scaffold(
        containerColor = NurBg,
        bottomBar = {
            if (currentScreen == "MAIN") {
                MainBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (currentTab != tab) {
                            currentTab = tab
                            // Trigger event for interstitial check (only if 60s passed & 3+ events)
                            AdManager.triggerEvent(activity)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "MONTHLY_SCHEDULE" -> {
                    ScheduleMonthScreen(
                        city = selectedCity,
                        calculationMethod = preferenceManager.calculationMethod,
                        onBack = { currentScreen = "MAIN" }
                    )
                }
                "QURAN_DETAIL" -> {
                    QuranDetailScreen(
                        surahNumber = selectedSurahNumber,
                        preferenceManager = preferenceManager,
                        onBack = { currentScreen = "MAIN" }
                    )
                }
                "SETTINGS" -> {
                    SettingsScreen(
                        currentCity = selectedCity,
                        preferenceManager = preferenceManager,
                        onPickCityClick = { isCityPickerOpen = true },
                        onThemeChanged = onThemeChanged,
                        onBack = { currentScreen = "MAIN" }
                    )
                }
                else -> {
                    when (currentTab) {
                        ScreenTab.HOME -> {
                            HomeScreen(
                                selectedCity = selectedCity,
                                preferenceManager = preferenceManager,
                                onCityClick = { isCityPickerOpen = true },
                                onSettingsClick = { currentScreen = "SETTINGS" },
                                onNavigateToQuran = { currentTab = ScreenTab.QURAN },
                                onNavigateToSholat = { currentTab = ScreenTab.SHOLAT },
                                onNavigateToDoa = { currentTab = ScreenTab.DOA }
                            )
                        }
                        ScreenTab.QURAN -> {
                            QuranListScreen(
                                preferenceManager = preferenceManager,
                                onSurahClick = { surahNo ->
                                    selectedSurahNumber = surahNo
                                    currentScreen = "QURAN_DETAIL"
                                }
                            )
                        }
                        ScreenTab.SHOLAT -> {
                            SholatScreen(
                                city = selectedCity,
                                preferenceManager = preferenceManager,
                                onCityClick = { isCityPickerOpen = true },
                                onSettingsClick = { currentScreen = "SETTINGS" },
                                onViewMonthlyClick = {
                                    AdManager.triggerEvent(activity) {
                                        currentScreen = "MONTHLY_SCHEDULE"
                                    }
                                }
                            )
                        }
                        ScreenTab.DOA -> {
                            DoaScreen(preferenceManager = preferenceManager)
                        }
                        ScreenTab.ZAKAT -> {
                            ZakatScreen(preferenceManager = preferenceManager)
                        }
                    }
                }
            }

            if (isCityPickerOpen) {
                CitySelectionBottomSheet(
                    currentCityId = selectedCity.id,
                    onCitySelected = { city ->
                        selectedCity = city
                        preferenceManager.cityId = city.id
                    },
                    onDismiss = { isCityPickerOpen = false }
                )
            }
        }
    }
}
