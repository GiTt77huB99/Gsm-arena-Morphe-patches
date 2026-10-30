package app.gsmarena.patches.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * The GSMArena app is not obfuscated, so every app-owned target is matched by
 * its exact, human readable class and method names (verified against
 * GSMArena 1.9.1, versionCode 191).
 *
 * Targets verified in `com.gsmarena.android.utility.AdUtils`:
 *   initializeMobileAdsSdk(Context)V            (static, SDK entry point)
 *   isMobileAdsSdkInitialized()Z                (static)
 *   canLoadAds()Z                               (private, master load gate)
 *   adsDisabledByUser(Context)Z                 (private, user preference gate)
 *   isBannerFrequencyAllowed()Z                 (private)
 *   isInterstitialFrequencyAllowed()Z           (private)
 *   shouldLoadBannerBasedOnServerFrequency()Z   (private)
 *   shouldLoadinterstitialBasedOnServerFrequency()Z (private)
 *   loadFullScreenAd(Activity)V
 *   showFullScreenAd()Z
 *   showBannerAd(AdView)V
 *   showPublisherBannerAd(AdManagerAdView)V
 *   showPublisherBannerInContainer(Activity, ViewGroup, Z)V
 *   preparePublisherBannerAdView(Context, AdManagerAdView, Z)V  (static)
 *   bindPublisherBannerAdUnit(Context, AdManagerAdView)V        (static)
 *   wirePublisherBannerConsentGuard(Context, AdManagerAdView)V  (static)
 */
private const val AD_UTILS = "Lcom/gsmarena/android/utility/AdUtils;"

object InitializeMobileAdsSdkFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "initializeMobileAdsSdk",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

object IsMobileAdsSdkInitializedFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "isMobileAdsSdkInitialized",
    returnType = "Z"
)

object CanLoadAdsFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "canLoadAds",
    returnType = "Z"
)

object AdsDisabledByUserFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "adsDisabledByUser",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;")
)

object IsBannerFrequencyAllowedFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "isBannerFrequencyAllowed",
    returnType = "Z"
)

object IsInterstitialFrequencyAllowedFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "isInterstitialFrequencyAllowed",
    returnType = "Z"
)

object ShouldLoadBannerBasedOnServerFrequencyFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "shouldLoadBannerBasedOnServerFrequency",
    returnType = "Z"
)

object ShouldLoadInterstitialBasedOnServerFrequencyFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "shouldLoadinterstitialBasedOnServerFrequency",
    returnType = "Z"
)

object LoadFullScreenAdFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "loadFullScreenAd",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;")
)

object ShowFullScreenAdFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "showFullScreenAd",
    returnType = "Z"
)

object ShowBannerAdFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "showBannerAd",
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/AdView;")
)

object ShowPublisherBannerAdFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "showPublisherBannerAd",
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/admanager/AdManagerAdView;")
)

object ShowPublisherBannerInContainerFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "showPublisherBannerInContainer",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;", "Landroid/view/ViewGroup;", "Z")
)

object PreparePublisherBannerAdViewFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "preparePublisherBannerAdView",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/google/android/gms/ads/admanager/AdManagerAdView;",
        "Z"
    )
)

object BindPublisherBannerAdUnitFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "bindPublisherBannerAdUnit",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/google/android/gms/ads/admanager/AdManagerAdView;"
    )
)

object WirePublisherBannerConsentGuardFingerprint : Fingerprint(
    definingClass = AD_UTILS,
    name = "wirePublisherBannerConsentGuard",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/google/android/gms/ads/admanager/AdManagerAdView;"
    )
)

/**
 * Verified in `com.gsmarena.android.utility.AdsConsentController` (Google UMP
 * consent orchestration used by SplashActivity, NotificationActivity and
 * GSMArenaNoAdActivity):
 *
 *   gatherConsentAndInitAds(Activity, Runnable)V  - runs the UMP consent flow,
 *       then invokes the continuation. Callers rely on the continuation being
 *       invoked, so this method must never simply return.
 *   canRequestAdsForAdLoads(Context)Z             - master gate for ad loads.
 *   shouldShowAdConsentMenuItem(Context)Z         - shows the consent menu item.
 */
private const val CONSENT_CONTROLLER = "Lcom/gsmarena/android/utility/AdsConsentController;"

object GatherConsentAndInitAdsFingerprint : Fingerprint(
    definingClass = CONSENT_CONTROLLER,
    name = "gatherConsentAndInitAds",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;", "Ljava/lang/Runnable;")
)

object CanRequestAdsForAdLoadsFingerprint : Fingerprint(
    definingClass = CONSENT_CONTROLLER,
    name = "canRequestAdsForAdLoads",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;")
)

object ShouldShowAdConsentMenuItemFingerprint : Fingerprint(
    definingClass = CONSENT_CONTROLLER,
    name = "shouldShowAdConsentMenuItem",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;")
)

/**
 * Verified in `com.gsmarena.android.utility.AnalyticsUtils`. Every tracking
 * method funnels into FirebaseAnalytics.logEvent, so no-oping them removes all
 * in-app event reporting (screen/page tracking, notification events, settings
 * events). Firebase Analytics itself is deactivated at the manifest level by
 * the analytics patch.
 */
private const val ANALYTICS_UTILS = "Lcom/gsmarena/android/utility/AnalyticsUtils;"

object TrackEventFingerprint : Fingerprint(
    definingClass = ANALYTICS_UTILS,
    name = "trackEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;")
)

object TrackEventSinglePageFingerprint : Fingerprint(
    definingClass = ANALYTICS_UTILS,
    name = "trackEventSinglePage",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;")
)

object TrackNotificationReceivedEventFingerprint : Fingerprint(
    definingClass = ANALYTICS_UTILS,
    name = "trackNotificationReceivedEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;")
)

object TrackDarkModeSettingChangedByUserEventFingerprint : Fingerprint(
    definingClass = ANALYTICS_UTILS,
    name = "trackDarkModeSettingChangedByUserEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;")
)

object TrackNotificationChannelPreferenceChangeEventFingerprint : Fingerprint(
    definingClass = ANALYTICS_UTILS,
    name = "trackNotificationChanelPreferenceChangeEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Z")
)

/**
 * The app bundles the full Google Mobile Ads SDK. These fingerprints match the
 * SDK's own entry points so no ad request can ever be created, even if a
 * future app update adds a new call path the app-level patches above do not
 * cover. The fingerprint approach is borrowed from the GPL-3.0 "adobo" patch
 * bundle (https://github.com/jkennethcarino/adobo); the SDK strings are stable
 * across apps and SDK releases.
 *
 * These are matched tolerantly (never fail the patch) because the exact SDK
 * version bundled inside the app can change between app updates.
 */
internal object AdLoadFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "L", "L", "L"),
    filters = listOf(
        string("Context cannot be null."),
        string(
            // AppOpenAd's `load` method has "adUnitId" instead of "AdUnitId".
            string = "dUnitId cannot be null.",
            comparison = StringComparisonType.ENDS_WITH
        ),
        string("#008 Must be called on the main UI thread.")
    )
)

internal object AdLoaderLoadAdFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string(
            string = "Failed to load ad",
            comparison = StringComparisonType.STARTS_WITH
        ),
        methodCall(
            definingClass = "Lcom/google/android/gms/ads/internal/util/client/",
            location = MatchAfterImmediately()
        )
    )
)

internal object AppOpenAdLoadFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/appopen/AppOpenAd;",
    name = "load",
    returnType = "V"
)

internal object ShowFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/internal/ads/",
    name = "show",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;")
)

internal object BaseAdViewFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/",
    superclass = "Landroid/view/ViewGroup;",
    returnType = "V",
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
    ),
    strings = listOf("#008 Must be called on the main UI thread.")
)

internal object BannerAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/",
    returnType = "V",
    strings = listOf("The ad size and ad unit ID must be set before loadAd is called.")
)

internal val abstractAdViewAdapterFingerprints =
    setOf(
        "requestBannerAd",
        "requestInterstitialAd",
        "requestNativeAd",
        "showInterstitial",
    ).map { methodName ->
        Fingerprint(
            definingClass = "Lcom/google/ads/mediation/AbstractAdViewAdapter;",
            name = methodName,
            returnType = "V"
        )
    }

internal val mediationFingerprints =
    setOf(
        " does not support app open ads.",
        " does not support banner ads.",
        " does not support interstitial ads.",
        " does not support native ads.",
        " does not support rewarded ads.",
        " does not support rewarded interstitial ads.",
    ).map { adString ->
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/mediation/",
            returnType = "V",
            filters = listOf(
                string(adString),
                string("com.google.android.gms.ads")
            )
        )
    }
