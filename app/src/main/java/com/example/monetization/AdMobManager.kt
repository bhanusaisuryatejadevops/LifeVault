package com.example.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {

    private const val TAG = "LifeVaultAdMob"

    // Official Google AdMob sample test ad unit IDs
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var lastInterstitialShownTime: Long = 0L
    private var transitionCount = 0

    // Frequency capping: at least 4 minutes between interstitials & at least 3 transitions
    private const val MIN_INTERSTITIAL_INTERVAL_MILLIS = 4 * 60 * 1000L
    private const val MIN_TRANSITIONS_REQUIRED = 3

    var isInitialized = false
        private set

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initStatus ->
                isInitialized = true
                Log.d(TAG, "AdMob initialized successfully: ${initStatus.adapterStatusMap}")
                preloadInterstitial(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization failed gracefully: ${e.message}")
        }
    }

    fun getBannerAdUnitId(context: Context): String {
        val prefs = context.getSharedPreferences("lifevault_prefs", Context.MODE_PRIVATE)
        val customId = prefs.getString("custom_banner_ad_id", null)
        return if (!customId.isNullOrBlank()) customId else TEST_BANNER_ID
    }

    fun getInterstitialAdUnitId(context: Context): String {
        val prefs = context.getSharedPreferences("lifevault_prefs", Context.MODE_PRIVATE)
        val customId = prefs.getString("custom_interstitial_ad_id", null)
        return if (!customId.isNullOrBlank()) customId else TEST_INTERSTITIAL_ID
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = getInterstitialAdUnitId(context)

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.w(TAG, "Interstitial failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Checks frequency capping and presents the interstitial ad at a natural transition
     * (e.g. Navigating back from Detail screen after reading).
     * Never interrupts editing, scanning, or saving workflows.
     */
    fun showInterstitialAtNaturalTransition(
        activity: Activity,
        onAdDismissed: () -> Unit
    ) {
        transitionCount++
        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastInterstitialShownTime

        if (transitionCount >= MIN_TRANSITIONS_REQUIRED &&
            timeSinceLast >= MIN_INTERSTITIAL_INTERVAL_MILLIS &&
            interstitialAd != null
        ) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    transitionCount = 0
                    preloadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    preloadInterstitial(activity)
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            // Did not meet frequency cap or ad not loaded yet; proceed immediately
            onAdDismissed()
        }
    }
}
