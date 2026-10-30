package app.gsmarena.patches.privacy

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.util.returnEarly
import app.gsmarena.patches.ads.TrackDarkModeSettingChangedByUserEventFingerprint
import app.gsmarena.patches.ads.TrackEventFingerprint
import app.gsmarena.patches.ads.TrackEventSinglePageFingerprint
import app.gsmarena.patches.ads.TrackNotificationChannelPreferenceChangeEventFingerprint
import app.gsmarena.patches.ads.TrackNotificationReceivedEventFingerprint
import app.gsmarena.patches.shared.COMPATIBILITY_GSMARENA

/**
 * No-ops every tracking method of `com.gsmarena.android.utility.AnalyticsUtils`
 * (used by 15 app classes), so no Firebase event is ever logged from app code,
 * regardless of the manifest flags.
 */
internal val disableAnalyticsEvents = bytecodePatch {
    execute {
        TrackEventFingerprint.method.returnEarly()
        TrackEventSinglePageFingerprint.method.returnEarly()
        TrackNotificationReceivedEventFingerprint.method.returnEarly()
        TrackDarkModeSettingChangedByUserEventFingerprint.method.returnEarly()
        TrackNotificationChannelPreferenceChangeEventFingerprint.method.returnEarly()
    }
}

/**
 * Removes the App Measurement broadcast receiver and services from the
 * manifest (AppMeasurementReceiver, AppMeasurementService,
 * AppMeasurementJobService).
 */
internal val removeAppMeasurementComponents = resourcePatch {
    execute {
        androidManifest {
            removeReceiver("""com\.google\.android\.gms\.measurement\..+Receiver""")
            removeService("""com\.google\.android\.gms\.measurement\..+Service""")
        }
    }
}

@Suppress("unused")
val disableFirebaseAnalyticsPatch = resourcePatch(
    name = "Disable Firebase Analytics",
    description = "Deactivates Firebase Analytics collection (events, screen tracking, " +
        "device identifiers), removes App Measurement services, and no-ops the app's " +
        "own AnalyticsUtils tracking methods. Push notifications keep working.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GSMARENA)
    category("privacy")

    dependsOn(
        disableAnalyticsEvents,
        removeAppMeasurementComponents,
    )

    execute {
        androidManifest {
            metaData(
                // Master switch: Analytics stops collecting and uploading.
                "firebase_analytics_collection_deactivated" to "true",
                // Belt and suspenders in case the deactivation flag is ever
                // overridden by a future Firebase SDK version.
                "firebase_analytics_collection_enabled" to "false",
                // Do not collect the advertising ID (GAID) for analytics.
                "google_analytics_adid_collection_enabled" to "false",
                // Do not collect the SSAID (Android device ID) for analytics.
                "google_analytics_ssaid_collection_enabled" to "false",
                // Disable automatic screen view reporting.
                "google_analytics_automatic_screen_reporting_enabled" to "false",
            )
        }
    }
}

@Suppress("unused")
val disableCrashlyticsPatch = resourcePatch(
    name = "Disable Crashlytics and Sessions",
    description = "Turns off Firebase Crashlytics and Firebase Sessions crash reporting " +
        "and removes their component registrations, so no crash data ever leaves the " +
        "device.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GSMARENA)
    category("privacy")

    execute {
        androidManifest {
            metaData(
                // Crashlytics collection switch.
                "firebase_crashlytics_collection_enabled" to "false",
                // Firebase Sessions (the crash pipeline Crashlytics uses).
                "firebase_sessions_enabled" to "false",
            )

            // Unregister the SDK components so they are never instantiated.
            removeFirebaseComponents(
                """com\.google\.firebase\.crashlytics\..+""",
                """com\.google\.firebase\.sessions\..+""",
                """com\.google\.firebase\.crash\.component\..+""",
            )

            removeService("""com\.google\.firebase\.sessions\.SessionLifecycleService""")
        }
    }
}

@Suppress("unused")
val disableFirebasePerformancePatch = resourcePatch(
    name = "Disable Firebase Performance",
    description = "Turns off Firebase Performance Monitoring and removes its component " +
        "registration, so no performance traces or network request metrics are collected.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GSMARENA)
    category("privacy")

    execute {
        androidManifest {
            metaData(
                "firebase_performance_collection_deactivated" to "true",
            )

            removeFirebaseComponents(
                """com\.google\.firebase\.perf\..+""",
            )
        }
    }
}

@Suppress("unused")
val removeAdvertisingIdPatch = resourcePatch(
    name = "Remove advertising ID permission",
    description = "Removes the AD_ID and Privacy Sandbox (ACCESS_ADSERVICES_*) permissions " +
        "from the manifest, so the app can no longer read the advertising ID or take part " +
        "in the Android ads services (Topics / Attribution) framework. Safe because all " +
        "ads are already removed.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GSMARENA)
    category("privacy")

    execute {
        androidManifest {
            removeUsesPermission(
                """com\.google\.android\.gms\.permission\.AD_ID(\.NOTIFICATION)?""",
                """android\.permission\.ACCESS_ADSERVICES_(AD_ID|ATTRIBUTION|TOPICS)""",
            )
        }
    }
}
