# 🧩 GSMArena Morphe Patches

Patches for [Morphe](https://morphe.software) that strip **all ads and all Google trackers** from the
official **GSMArena** Android app (`com.gsmarena.android`), developed and verified against
**GSMArena 1.9.1** (versionCode 191, the latest Play Store release).

## 🩹 Patches

| Patch | Category | What it does |
|---|---|---|
| **Remove ads** | ads | Disables AdMob and Google Ad Manager banners, interstitials, the Mobile Ads SDK init, and the whole Google UMP consent flow (the splash screen continuation is preserved, so the app never hangs). Also hides the "Ad consent" menu item. |
| **Disable Firebase Analytics** | privacy | Deactivates Firebase Analytics collection, removes App Measurement services, and no-ops all `AnalyticsUtils` tracking calls (page events, notification events, settings events). |
| **Disable Crashlytics and Sessions** | privacy | Turns off Firebase Crashlytics + Sessions crash upload and unregisters their components. |
| **Disable Firebase Performance** | privacy | Turns off Firebase Performance Monitoring and unregisters its component. |
| **Remove advertising ID permission** | privacy | Drops the `AD_ID` and `ACCESS_ADSERVICES_*` (Privacy Sandbox) permissions, so the app cannot read your advertising ID at all. |

**Left fully functional:** push notifications (Firebase Messaging), the news feed, spec search,
comparisons, comments, dark mode, data saver — everything except ads and telemetry.

## 📲 How to use these patches on your phone (Morphe for Android)

The Morphe app loads patch bundles from GitHub sources, so the one-time setup is:

1. **Create your own copy of this repository on GitHub** (or push this folder to a new repo,
   e.g. `your-username/gsmarena-morphe-patches`).
2. **Enable GitHub Actions** for the repo. The included `release.yml` workflow builds the
   patch bundle and publishes it as a release — no secrets needed (it uses the built-in
   `GITHUB_TOKEN`). Pushing to `main` (or a `fix:` / `feat:` commit) creates a release;
   pushing to `dev` creates a pre-release.
3. In the **Morphe app** on your phone:
   - Open **Add patch source** (in Settings or via the source icon), choose **GitHub**,
     and enter `your-username/gsmarena-morphe-patches`.
   - Or simply open this link on the phone:
     `https://morphe.software/add-source?github=your-username/gsmarena-morphe-patches`
4. **Get the original app file**: GSMArena **1.9.1**, the **(universal) (120-640dpi)** variant,
   from [APKMirror](https://www.apkmirror.com/apk/gsmarena-com/gsmarena/) (Morphe also guides
   you to it when you select the app — the bundle declares the APK file type for exactly that
   variant).
5. Select **GSMArena** in Morphe, pick the patches (all are enabled by default), tap **Patch**,
   then install the result like any normal APK.
6. If GSMArena was installed before, **uninstall the original first** — the patched build has a
   different signature and Android will refuse to install it as an update.

## 🚀 Building the bundle locally

Requirements: **JDK 17 or 21** (21 recommended) and a **GitHub Personal Access Token** (classic),
because Morphe's Gradle plugin and patcher are distributed via GitHub Packages, which requires
authentication even for public packages. The token needs the `read:packages` scope only.

Create a token at https://github.com/settings/tokens, then add it to
`~/.gradle/gradle.properties`:

```properties
gpr.user=your-github-username
gpr.key=ghp_yourPersonalAccessToken
```

Then build:

```bash
./gradlew buildAndroid
```

The patch bundle is written to `patches/build/libs/patches-*.mpp`.

You can apply that local `.mpp` file with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop),
or just let the GitHub Actions release (section above) build it for your phone.

## 🧑‍💻 Development notes

- `patches/src/main/kotlin/app/gsmarena/patches/` contains the patches:
  - `ads/` — ad removal, consent flow removal, Mobile Ads SDK kill switch
    (`Fingerprints.kt` documents every verified target).
  - `privacy/` — Firebase Analytics / Crashlytics / Performance / advertising ID removal.
  - `shared/Constants.kt` — the compatibility declaration (app name, package, versions).
- `PATCH_NOTES.md` documents the reverse-engineered internals of GSMArena 1.9.1 the patches
  rely on — read it before supporting a new app version.
- All app-owned targets are matched by **exact unobfuscated class/method names**, so minor
  app updates usually keep working. Any future version can be patched **experimentally**
  (declared in `Constants.kt`); if a fingerprint misses, the patch fails loudly instead of
  silently doing nothing.
- Releases are fully automated with semantic-release: use `feat:` / `fix:` / `chore:` commit
  prefixes. Do not create releases by hand.

## ❓ FAQ

**Will I still get push notifications?**
Yes. Firebase Messaging and its dependencies are untouched — only Analytics, Crashlytics,
Performance, Sessions, and ads are disabled.

**Why does the app still show an empty space where banners were?**
It should not — the app's own banner containers collapse when no view is added. If you find a
screen that reserves space, please open an issue with the screen name.

**Does this work on version X newer than 1.9.1?**
The bundle declares any future version as *experimental*. Try it; if a patch fails, update the
fingerprints per `PATCH_NOTES.md` and release a `fix:` commit.

**Play Protect warns about the patched APK.**
That is expected for any sideloaded app; the APK was patched on your own device from the
original file you supplied.

## 📜 License

GPL-3.0. The Google Mobile Ads SDK kill-switch fingerprints are adapted from
[jkennethcarino/adobo](https://github.com/jkennethcarino/adobo) (GPL-3.0) and the project
structure follows the official
[MorpheApp/morphe-patches-template](https://github.com/MorpheApp/morphe-patches-template).
