package app.gsmarena.patches.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.util.returnEarly
import app.gsmarena.patches.privacy.androidManifest
import app.gsmarena.patches.privacy.metaData
import app.gsmarena.patches.privacy.removeActivity
import app.gsmarena.patches.privacy.removeService
import app.gsmarena.patches.shared.COMPATIBILITY_GSMARENA

/**
 * Never fails the patch if a Google Mobile Ads SDK entry point cannot be
 * matched. The SDK version bundled inside the app changes between app
 * releases, while the app-owned targets are matched strictly.
 */
private fun BytecodePatchContext.returnEarlySafe(fingerprint: Fingerprint) {
    runCatching {
        fingerprint.method.returnEarly()
    }
}

/**
 * Disables the Google UMP ad consent orchestration without breaking the app
 * startup flow.
 *
 * `gatherConsentAndInitAds(Activity, Runnable)` is invoked by SplashActivity,
 * NotificationActivity and GSMArenaNoAdActivity with a continuation that must
 * run for the UI to proceed. The method body is therefore prefixed with code
 * that immediately runs the continuation and returns; the original consent
 * logic becomes unreachable dead code:
 *
 *     invoke-interface { p1 }, Ljava/lang/Runnable;->run()V
 *     return-void
 *
 * `canRequestAdsForAdLoads` returning false additionally short-circuits every
 * remaining ad load decision ("Skipping ad load: UMP canRequestAds() is
 * false."), and the "Ad consent" overflow menu item is hidden.
 */
internal val disableAdConsentFlow = bytecodePatch {
    execute {
        GatherConsentAndInitAdsFingerprint.method.addInstructions(
            0,
            """
                invoke-interface { p1 }, Ljava/lang/Runnable;->run()V
                return-void
            """
        )

        CanRequestAdsForAdLoadsFingerprint.method.returnEarly(false)
        ShouldShowAdConsentMenuItemFingerprint.method.returnEarly(false)
    }
}

/**
 * Removes the AdMob / Google Ad Manager components from the manifest so
 * nothing ad related is reachable from the system side. This is safe because
 * every bytecode path into the Mobile Ads SDK is already no-op'd:
 *
 * - AdActivity, OutOfContextTestingActivity, NotificationHandlerActivity
 * - AdService
 *
 * The `com.google.android.gms.ads.APPLICATION_ID` meta-data is intentionally
 * left in place: it is never read again, and keeping it means that even a
 * hypothetical missed code path would fail a null check instead of throwing.
 */
internal val removeAdManifestComponents = resourcePatch {
    execute {
        androidManifest {
            metaData(
                "com.google.android.gms.ads.AD_MANAGER_APP" to "false"
            )
            removeActivity(
                """com\.google\.android\.gms\.ads\.AdActivity""",
                """com\.google\.android\.gms\.ads\.OutOfContextTestingActivity""",
                """com\.google\.android\.gms\.ads\.NotificationHandlerActivity""",
            )
            removeService("""com\.google\.android\.gms\.ads\.AdService""")
        }
    }
}

/**
 * Turns the user preference "ads disabled" permanently on, so the app's own
 * cleanup paths (destroy banner, clear container) behave exactly as when a
 * user opted out of ads inside the app settings, and no ad is ever loaded or
 * shown again.
 */
private fun BytecodePatchContext.disableAllAppAdPaths() {
    // Master gates.
    CanLoadAdsFingerprint.method.returnEarly(false)
    AdsDisabledByUserFingerprint.method.returnEarly(true)
    IsMobileAdsSdkInitializedFingerprint.method.returnEarly(false)

    // Frequency caps become irrelevant, but keep them closed anyway.
    IsBannerFrequencyAllowedFingerprint.method.returnEarly(false)
    IsInterstitialFrequencyAllowedFingerprint.method.returnEarly(false)
    ShouldLoadBannerBasedOnServerFrequencyFingerprint.method.returnEarly(false)
    ShouldLoadInterstitialBasedOnServerFrequencyFingerprint.method.returnEarly(false)

    // SDK initialization never happens.
    InitializeMobileAdsSdkFingerprint.method.returnEarly()

    // Full screen (interstitial) ads.
    LoadFullScreenAdFingerprint.method.returnEarly()
    ShowFullScreenAdFingerprint.method.returnEarly(false)

    // Classic AdMob banners.
    ShowBannerAdFingerprint.method.returnEarly()

    // Google Ad Manager publisher banners (the inline banners on
    // device/review pages) and their setup paths.
    ShowPublisherBannerAdFingerprint.method.returnEarly()
    ShowPublisherBannerInContainerFingerprint.method.returnEarly()
    PreparePublisherBannerAdViewFingerprint.method.returnEarly()
    BindPublisherBannerAdUnitFingerprint.method.returnEarly()
    WirePublisherBannerConsentGuardFingerprint.method.returnEarly()
}

/**
 * Kills every Google Mobile Ads SDK entry point (ad loading, showing,
 * mediation adapters). Tolerant matching, since the SDK code inside the app
 * changes with SDK updates.
 */
private fun BytecodePatchContext.disableGoogleMobileAdsSdk() {
    abstractAdViewAdapterFingerprints.forEach { fingerprint ->
        returnEarlySafe(fingerprint)
    }

    mediationFingerprints.forEach { fingerprint ->
        returnEarlySafe(fingerprint)
    }

    setOf(
        AdLoadFingerprint,
        AdLoaderLoadAdFingerprint,
        ShowFingerprint,
    ).forEach { fingerprint ->
        runCatching {
            fingerprint.matchAll().forEach { match ->
                match.method.returnEarly()
            }
        }
    }

    setOf(
        AppOpenAdLoadFingerprint,
        BaseAdViewFingerprint,
        BannerAdFingerprint,
    ).forEach { fingerprint ->
        returnEarlySafe(fingerprint)
    }
}

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Disables all ads (AdMob and Ad Manager banners, interstitials) and the " +
        "Google UMP consent flow in the GSMArena app. The startup splash screen " +
        "continuation is preserved, so nothing hangs.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GSMARENA)
    category("ads")

    dependsOn(
        disableAdConsentFlow,
        removeAdManifestComponents,
    )

    execute {
        disableAllAppAdPaths()
        disableGoogleMobileAdsSdk()
    }
}
