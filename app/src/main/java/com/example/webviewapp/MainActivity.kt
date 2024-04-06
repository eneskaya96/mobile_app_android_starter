package com.example.webviewapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.rewarded.RewardedAd

import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

import kotlinx.coroutines.*


class MainActivity : ComponentActivity() {

    private var rewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleAdvertisingIdAndLogging()
        MobileAds.initialize(this) {}

        loadRewardedAd()

        setContent {
            MainContent(BuildConfig.WEB_VIEW_APP_URL,  onAdShowRequested = { showRewardedAd() })
            /*
            AppsFlyerLib.getInstance().logEvent(
                LocalContext.current,
                "app_opened",
                mapOf("key" to "value")
            )
            */
        }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(this, BuildConfig.ADMOB_REWARDED_ADD_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                this@MainActivity.rewardedAd = rewardedAd
            }
        })
    }

    fun showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(this) { rewardItem ->
                var reward = rewardItem.amount;
                rewardedAd = null;
                loadRewardedAd()
            }
        } else {
            loadRewardedAd()
        }
    }

    private fun handleAdvertisingIdAndLogging() {
        CoroutineScope(Dispatchers.Main).launch {
            val adId = getAdvertisingId()
            /*
            AppsFlyerLib.getInstance().logEvent(
                this@MainActivity,
                "$adId",
                mapOf("IDFA" to "$adId")
            )

             */
        }
    }

    private suspend fun getAdvertisingId(): String = withContext(Dispatchers.IO) {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
            return@withContext adInfo.id ?: "Advertising ID could not be retrieved"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Error retrieving Advertising ID"
        }
    }
}

@Composable
fun MainContent(url: String, onAdShowRequested: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        WebViewExample(url, Modifier.weight(1f), onAdShowRequested)
        AdComposable()
    }
}

@SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
@Composable
fun WebViewExample(urlToRender: String, modifier: Modifier = Modifier, onAdShowRequested: () -> Unit) {
    var tapCount = 3
    val context = LocalContext.current
    val gestureDetector = remember {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                tapCount += 1;
                if(tapCount >= 5) {
                    onAdShowRequested()
                    tapCount = 0;
                }
                return true
            }
        })
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadUrl(urlToRender)

                setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    false
                }
            }
        }
    )
}

@Composable
fun AdComposable() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { ctx ->
            AdView(ctx).also { adView ->
                adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                adView.adUnitId = BuildConfig.ADMOB_BANNER_ID
                adView.loadAd(AdRequest.Builder().build())
            }
        }
    )
}
