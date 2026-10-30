package util

import java.io.File
import java.util.Properties

fun main() {
    var dir: File = File(System.getProperty("user.dir"))
    while (!File(dir, "gradle.properties").exists()) {
        dir = dir.parentFile ?: error("gradle.properties not found above working directory")
    }
    val version = Properties().apply {
        File(dir, "gradle.properties").inputStream().use { load(it) }
    }.getProperty("version", "0.0.0")

    val pkg = """
      "compatiblePackages": [ { "packageName": "com.gsmarena.android", "name": "GSMArena",
        "formFactor": null, "targets": [
          { "version": "1.9.1", "isExperimental": false, "description": null },
          { "version": null, "isExperimental": true, "description": null } ] } ]
    """.trimIndent()

    val patch = { n: String, d: String ->
        """
      { "name": "$n", "description": "$d", "options": [],
        $pkg }
        """.trimIndent()
    }

    val patches = listOf(
        patch("Remove ads", "Disables all ads (AdMob and Ad Manager banners, interstitials) and the Google UMP consent flow. The splash screen continuation is preserved, so nothing hangs."),
        patch("Disable Firebase Analytics", "Deactivates Firebase Analytics collection, removes App Measurement services and no-ops the app's own tracking methods. Push notifications keep working."),
        patch("Disable Crashlytics and Sessions", "Turns off Firebase Crashlytics and Sessions crash reporting and removes their component registrations."),
        patch("Disable Firebase Performance", "Turns off Firebase Performance Monitoring and removes its component registration."),
        patch("Remove advertising ID permission", "Removes the AD_ID and ACCESS_ADSERVICES_* permissions so the app cannot read the advertising ID.")
    )

    val json = """{
  "NOTE": "Do NOT manually edit this file. This file is automatically updated when semantic release (release.yml) runs.",
  "version": "$version",
  "patches": [
 ${patches.joinToString(",\n")}
  ]
}
"""
    File(dir, "patches-list.json").writeText(json)
    println("Wrote patches-list.json (version=$version, ${patches.size} patches)")
}
