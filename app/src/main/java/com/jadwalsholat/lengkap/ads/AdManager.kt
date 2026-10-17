package com.jadwalsholat.lengkap.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdManager {
    private const val TAG = "AdManager"

    // TEST AD UNIT IDs (GANTI KE PRODUKSI SEBELUM RELEASE KE PLAY STORE)
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var lastInterstitialTime: Long = 0
    private var eventCount: Int = 0

    // Cooldown 60 detik sesuai panduan AdMob & Aturan Android
    private const val COOLDOWN_MILLIS = 60_000L
    private const val REQUIRED_EVENTS_COUNT = 3

    fun initialize(activity: Activity) {
        // Atur Content Rating ke G (General/Semua Umur) agar sesuai dengan aplikasi Islami
        val requestConfiguration = RequestConfiguration.Builder()
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        // UMP Consent Management (GDPR & Privasi Internasional)
        val consentParams = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            consentParams,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        MobileAds.initialize(activity) {
                            loadInterstitial(activity)
                        }
                    }
                }
            },
            { requestError ->
                Log.w(TAG, "Consent request error: ${requestError.message}")
                MobileAds.initialize(activity) {
                    loadInterstitial(activity)
                }
            }
        )

        // Preload pertama kali jika consent sudah ada
        if (consentInformation.canRequestAds()) {
            MobileAds.initialize(activity) {
                loadInterstitial(activity)
            }
        }
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.d(TAG, "Interstitial loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Memeriksa kedua syarat:
     * 1. Cooldown waktu minimal 60 detik
     * 2. Ambang batas interaksi (event count)
     */
    fun shouldShowInterstitial(): Boolean {
        val now = System.currentTimeMillis()
        val hasCooledDown = (now - lastInterstitialTime) >= COOLDOWN_MILLIS
        val hasReachedEvents = eventCount >= REQUIRED_EVENTS_COUNT
        return hasCooledDown && hasReachedEvents && interstitialAd != null
    }

    fun triggerEvent(activity: Activity, onComplete: () -> Unit = {}) {
        eventCount++
        if (shouldShowInterstitial()) {
            showInterstitial(activity, onComplete)
        } else {
            onComplete()
        }
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null && (System.currentTimeMillis() - lastInterstitialTime) >= COOLDOWN_MILLIS) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialTime = System.currentTimeMillis()
                    eventCount = 0
                    loadInterstitial(activity) // Preload next ad
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            onDismissed()
        }
    }
}
