# Reverse-engineering notes — GSMArena 1.9.1 (versionCode 191)

Everything in this file was verified by decompiling the base APK of the official
**1.9.1** release (the universal 120-640dpi variant) with apktool 2.12.1. Use this as the
checklist when adapting the patches to a newer app version.

## App shape

- Package: `com.gsmarena.android`, minSdk 21, targetSdk 36, 5 dex files.
- **Not obfuscated** — all app classes keep readable names.
- App code lives in `smali_classes4/com/gsmarena/android/` (223 classes).
- Bundled Google SDKs: Google Mobile Ads (incl. mediation adapters), Firebase
  (analytics, crashlytics, sessions, perf, messaging, installations, remote config,
  measurement), plus okhttp/retrofit/glide. No Facebook/AppLovin/Unity/Mintegral —
  **AdMob/Ad Manager is the only ad SDK**.

## Ad system

All ads funnel through one singleton: `com.gsmarena.android.utility.AdUtils`
(`smali_classes4/com/gsmarena/android/utility/AdUtils.smali`).

Key methods (signature → role):

| Method | Role |
|---|---|
| `static initializeMobileAdsSdk(Context)V` | Mobile Ads SDK init |
| `static isMobileAdsSdkInitialized()Z` | init guard |
| `private canLoadAds()Z` | master load gate |
| `private adsDisabledByUser(Context)Z` | reads the app's own ad preference (`GSMArenaAds` prefs) |
| `private isBannerFrequencyAllowed()Z` / `private isInterstitialFrequencyAllowed()Z` | local frequency caps |
| `private shouldLoadBannerBasedOnServerFrequency()Z` / `private shouldLoadinterstitialBasedOnServerFrequency()Z` | server-driven caps (strings `Server_nBannerFreq`, `Server_nInterstitialFreq`, `Server_bNonPersonalizedAds`) |
| `loadFullScreenAd(Activity)V` | prefetches the interstitial |
| `showFullScreenAd()Z` | shows the interstitial |
| `showBannerAd(AdView)V` | classic AdMob banner |
| `showPublisherBannerAd(AdManagerAdView)V` (+ private 2-arg overload) | Ad Manager banner |
| `showPublisherBannerInContainer(Activity, ViewGroup, Z)V` | adds publisher banner into a container |
| `static preparePublisherBannerAdView(Context, AdManagerAdView, Z)V` | sizes/prepares the banner |
| `static bindPublisherBannerAdUnit(Context, AdManagerAdView)V` | binds the ad unit |
| `static wirePublisherBannerConsentGuard(Context, AdManagerAdView)V` | consent guard wiring |

Ad unit IDs: the AdMob app id is `ca-app-pub-9508420272844734~5415305799`
(AndroidManifest meta-data); the publisher ad unit ids are delivered at runtime (no
`ca-app-pub-.../...` constants in the dex). Patching the SDK entry points makes them
irrelevant.

### Consent flow

`com.gsmarena.android.utility.AdsConsentController` orchestrates the Google UMP flow.

- Callers of `gatherConsentAndInitAds(Activity, Runnable)V`:
  `SplashActivity` (startup), `NotificationActivity`, `GSMArenaNoAdActivity`.
  **The `Runnable` continuation must run or the caller stalls** — the patch prefixes the
  method body with `invoke-interface { p1 }, Ljava/lang/Runnable;->run()V` +
  `return-void` instead of returnEarly.
- `canRequestAdsForAdLoads(Context)Z` is the master gate used by AdUtils
  (`"Skipping ad load: UMP canRequestAds() is false."` string in AdUtils).
- `shouldShowAdConsentMenuItem(Context)Z` controls the overflow menu entry.
- `resumePendingSplashIfConsentGranted(Activity)Z` is called from `AppUtils$4` on resume;
  it degrades gracefully when nothing is pending, so it is left untouched.

### Manifest ad components

- meta-data `com.google.android.gms.ads.APPLICATION_ID` = `ca-app-pub-9508420272844734~5415305799`
  (left in place on purpose), `AD_MANAGER_APP` = true (set to false by the patch).
- `com.google.android.gms.ads.AdActivity`, `.OutOfContextTestingActivity`,
  `.NotificationHandlerActivity`, `.AdService` (removed by the patch).

## Analytics / telemetry

- `com.gsmarena.android.utility.AnalyticsUtils` is used by 15 app classes. Every method
  calls `FirebaseAnalytics.logEvent(...)`:
  `trackEvent(String)`, `trackEventSinglePage(String)`,
  `trackNotificationReceivedEvent(String, String)`,
  `trackDarkModeSettingChangedByUserEvent(String)`,
  `trackNotificationChanelPreferenceChangeEvent(String, Z)` — note the app's own typo
  "Chanel". All are no-op'd by the patch.
- Manifest components removed: `AppMeasurementReceiver`, `AppMeasurementService`,
  `AppMeasurementJobService`.
- Crashlytics is referenced by GSMArenaQuickSearchFetch / GSMArenaPicViewerDataFetch
  (exception reporting); the `firebase_crashlytics_collection_enabled=false` flag plus
  component unregistration is sufficient, no code changes needed.
- Firebase **Messaging is intentionally kept** (push notifications are a core feature of
  the app). `MyApplication` fetches an FCM token via `FirebaseInstanceId.getToken()`.
- RemoteConfig is bundled but **not referenced by app code** (the `Server_*` values come
  from GSMArena's own API, not RemoteConfig), so it is inert and left alone.

## Version bump checklist

When a new GSMArena release drops:

1. Decompile the base APK (`apktool d base.apk`).
2. Re-verify the method lists in `AdUtils`, `AdsConsentController`, `AnalyticsUtils`
   (`rg "^\.method" <file>.smali`). Class/method names are unobfuscated — updates usually
   only add/remove methods.
3. Confirm `SplashActivity` still calls `gatherConsentAndInitAds` with a continuation.
4. Check the manifest for new ad/measurement components.
5. Update `AppTarget(version = "x.y.z")` in `shared/Constants.kt` and release a `feat:`
   (support bump) or `fix:` commit.
