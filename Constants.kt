package app.gsmarena.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * The GSMArena app the patches in this bundle are developed against.
 *
 * - Latest stable: 1.9.1 (versionCode 191), universal APK on APKMirror.
 * - `version = null` marks any future version as supported experimentally, so
 *   Morphe Manager still allows patching newer releases. The fingerprints are
 *   built to survive minor updates: all app-owned targets are matched by
 *   unobfuscated class and method names, and the Google Mobile Ads SDK targets
 *   are matched by stable SDK strings instead of class names.
 */
val COMPATIBILITY_GSMARENA = Compatibility(
    name = "GSMArena",
    packageName = "com.gsmarena.android",
    //
    // The 1.9.1 release on APKMirror is distributed as a single universal APK
    // ("(universal) (120-640dpi)"). Morphe web-search will send users there.
    //
    apkFileType = ApkFileType.APK,
    // GSMArena launcher icon background (near-black).
    appIconColor = 0x101010,
    targets = listOf(
        // Version confirmed 100% working (developed against this release).
        AppTarget(
            version = "1.9.1"
        ),
        // Any future version is supported experimentally.
        AppTarget(
            version = null,
            isExperimental = true
        )
    )
)
