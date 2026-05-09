# Songlink Share

Share a song from Spotify, YouTube Music, Apple Music, Tidal — whatever. Tap **Songlink Share** in the Android share sheet and get a [song.link](https://song.link) URL on your clipboard instantly. No app-switching, no copying and pasting
---
# why?

i use https://odesli.co/ on a quite regular basis and was becoming increasingly frustrated with having to copy the link of the song I want to send, go to my browser open the site and paste it in.So in short I’m lazy lol 

---

## How it works

1. Hit share on any song in any music app.
2. Pick **Songlink Share** from the share sheet.
3. Done. The universal link is already on your clipboard.

Under the hood it calls the [Odesli API](https://www.notion.so/API-d0ebe08a5e304a55928405eb682f6741) with your URL and grabs the `pageUrl` from the response. That's the one link that works for everyone regardless of what they're listening on.

---

## ai usage

it has been used to fix bugs as i am stupid.
it has also been used to orginize the code.
this readme was mostly ai as im lazy lmao
it has also been used to push changes as i dont know how git works fully and its constantly giving me errors:P

---

## Project structure

```
songlink-share/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml           # intent filters (ACTION_SEND + LAUNCHER)
│       ├── kotlin/com/songlink/share/
│       │   ├── MainActivity.kt           # handles incoming intents, hosts Compose
│       │   ├── ShareViewModel.kt         # state machine, API calls, clipboard write
│       │   ├── api/
│       │   │   ├── SonglinkApi.kt        # Retrofit interface
│       │   │   └── RetrofitClient.kt     # OkHttp + kotlinx.serialization
│       │   ├── intent/
│       │   │   └── IntentHandler.kt      # pulls the first URL out of a share payload
│       │   ├── model/
│       │   │   └── SonglinkResponse.kt   # response model + SonglinkState sealed class
│       │   └── ui/
│       │       ├── ShareScreen.kt        # Idle / Loading / Success / Error screens
│       │       └── theme/
│       │           ├── Color.kt
│       │           ├── Theme.kt
│       │           └── Type.kt
│       └── res/
│           ├── values/strings.xml
│           ├── values/themes.xml
│           └── xml/network_security_config.xml
├── gradle/
│   └── libs.versions.toml                # version catalog (AGP, Kotlin, Compose, Retrofit…)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Tech stack

| | |
|---|---|
| UI | Jetpack Compose + Material 3 |
| HTTP | Retrofit 2 + OkHttp 4 |
| JSON | kotlinx.serialization + Jakewharton converter |
| State | `ViewModel` + `StateFlow` |
| Clipboard | `ClipboardManager` |
| Min SDK | 26 (Android 8.0+) |

---

## Building

**Requirements:** Android Studio Ladybug (2024.2.1+), JDK 17, Android SDK API 35.

1. Clone the repo.
2. Open the `songlink-share/` folder in Android Studio.
3. Let Gradle sync finish.
4. Plug in a device (API 26+) or spin up an emulator.
5. Hit **Run**.

**Or from the command line:**

```bash
cd songlink-share
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

```bash
./gradlew installDebug   # build + install in one step
```

---

## Testing

**From Spotify:** open any song → ⋮ → Share → pick Songlink Share from the sheet.

**From YouTube Music:** any song or album → ⋮ → Share → Songlink Share.

**From Apple Music:** any song → ⋯ → Share Song… → Songlink Share.

**Without a music app (adb):**

```bash
adb shell am start \
  -a android.intent.action.SEND \
  -t text/plain \
  --es android.intent.extra.TEXT "https://open.spotify.com/track/6rqhFgbbKwnb9MLmUQDhG6" \
  com.songlink.share/.MainActivity
```

This fires the same intent Spotify sends when you tap Share.

---

## UI states

| State | |
|---|---|
| **Idle** | opened from the launcher, nothing to do |
| **Loading** | waiting on the API |
| **Success** | link copied to clipboard |
| **Error** | no URL in the share payload, network failure, bad response, or missing `pageUrl` |

---

## Permissions

`INTERNET` — to call the Songlink API. That's it.

---

Powered by [Songlink / Odesli](https://song.link).
