# Khushoo' (خشوع) • Focus & Prayer Call Shield

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="96" height="96" alt="Khushoo Logo" />
</p>

<p align="center">
  <b>A minimalist, peaceful, and bulletproof Android utility to quiet your phone during prayers, meetings, and deep focus.</b><br/>
  <i>Automatically rejects incoming calls and immediately sends a pre-set SMS reply — with zero sound, zero vibration, and zero disturbance.</i>
</p>

<p align="center">
  <a href="https://github.com/anesbo/khoshoo3/releases"><img src="https://img.shields.io/badge/Release-v1.0.0-black?style=for-the-badge&logo=android" alt="Version 1.0.0" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.0-black?style=for-the-badge&logo=kotlin" alt="Kotlin 2.0.0" /></a>
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-black?style=for-the-badge&logo=android" alt="Android API 26+" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-black?style=for-the-badge" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Design-True%20Black%20Minimal-black?style=for-the-badge" alt="True Black" />
</p>

---

## 📥 Direct Download & Installation

You can download and install the signed APK directly:

👉 **[Download Khushoo-v1.0.0.apk](apk/Khushoo-v1.0.0.apk)** *(~11.3 MB)*

1. Download `Khushoo-v1.0.0.apk` to your Android device.
2. Open the file and allow **"Install unknown apps"** if prompted by Android.
3. Launch **Khushoo'** and grant the required permissions (SMS & Phone Call screening).
4. Tap the central button or toggle the Quick Settings tile anytime you enter prayer or deep focus!

---

## ✨ Key Features

### 🛡️ 1. Bulletproof Call Shielding (Zero Bypass)
Unlike basic call screening apps that fail when an incoming call comes from an existing device contact (due to Android Telecom contact exemptions), Khushoo implements an advanced dual-layer interception engine:
* **Primary Layer**: Android `CallScreeningService` API with `setDisallowCall(true)` and `setRejectCall(true)`.
* **Secondary Real-time Listener**: Instant `TelephonyManager` state observer and `TelecomManager.endCall()` ensuring 100% rejection reliability for both saved contacts and unknown numbers.

### 🤫 2. Complete Sound & Notification Silence
* Incoming phone calls are immediately silenced via `TelecomManager.silenceRinger()`.
* Automatically zeroes `STREAM_NOTIFICATION`, `STREAM_RING`, and `STREAM_SYSTEM` volumes while active.
* Integrates with Android's **Do Not Disturb** (`ACCESS_NOTIFICATION_POLICY`), silencing OS-level heads-up notification popups and chimes.
* Khushoo's own foreground service runs on a silent `IMPORTANCE_LOW` channel without chimes or vibrations.

### 💬 3. Smart Auto-Reply SMS with Cooldown
* As soon as a call is rejected, an SMS reply is automatically dispatched to the caller.
* **Anti-Spam Cooldown Engine**: Remembers caller phone numbers and enforces a 2-minute cooldown so frequent repeat callers aren't spammed with multiple SMS messages.
* Includes 4 pre-built quick message templates:
  * 🕌 **Prayer**: *"I am currently praying and my phone is silenced. I will return your call shortly."*
  * 💼 **Meeting**: *"In an important meeting right now, will contact you once finished."*
  * 🚗 **Driving**: *"I am driving right now. Will call you back once I arrive safely."*
  * 🎯 **Deep Focus**: *"Deep focus mode active. Will reply as soon as possible."*
* Fully custom text message editor with real-time character counter and SMS segment indicator.

### 🖤 4. True Black Minimalist Aesthetic
* Designed from scratch in **True Black (`#000000`)** for modern OLED displays, maximum battery savings, and zero eye fatigue.
* Clean monochrome surfaces (`#171717` dark charcoal cards, crisp subtle dividers).
* Soft rose accent (`#E11D48`) exclusively for the active shield state.
* Includes a **Light Theme toggle** (Paper White `#FAFAFA`) for users who prefer clean bright surfaces.

### 📐 5. Streamlined Single-Screen Layout
Ordered intentionally for immediate access:
1. **Top**: Timer Duration Selector with quick presets (15m, 30m, 45m, 1h, and 00:00 Manual Mode).
2. **Middle**: The prominent circular activation button with real-time countdown display and breathing pulse indicator.
3. **Bottom**: Auto-reply message field and pre-set situation chips.
4. **Footer**: Shielded calls history drawer and author credits.

### ⚡ 6. Android Quick Settings Tile
* Add the **Khushoo'** tile to your notification quick panel.
* Toggle silent focus on and off with a single swipe and tap without needing to unlock or open the application.

### 🌍 7. Multi-Language & Native RTL Support
* Instant language switcher directly in the top app bar:
  * **العربية** (Arabic - Full native Right-To-Left layout & typography)
  * **English**
  * **Français** (French)

---

## 📱 Screenshots & UI Architecture

| Inactive (True Black) | Active Shield Mode | Timer & Message Controls |
| :---: | :---: | :---: |
| Clean monochrome ready state | Vibrant pulse rose active state | Top timer & bottom editable SMS |

---

## 🔒 Permissions & Privacy

Khushoo operates **100% offline** and does **not** collect, store, or transmit any user data. All processing happens entirely on your local device.

| Permission | Purpose |
| :--- | :--- |
| `SEND_SMS` | Required to send the automated reply SMS when a call is rejected. |
| `READ_PHONE_STATE` | Detects incoming call state so calls can be rejected immediately. |
| `READ_CALL_LOG` | Resolves caller number for contact calls to send the auto-reply. |
| `ANSWER_PHONE_CALLS` | Enables programmatic call hangup on Android 9.0+. |
| `ACCESS_NOTIFICATION_POLICY` | Manages Do Not Disturb for total silence during prayer/focus sessions. |
| `POST_NOTIFICATIONS` | Displays the silent ongoing countdown notification in the status bar. |

---

## 🛠️ Tech Stack & Architecture

* **Language**: [Kotlin](https://kotlinlang.org/) 2.0.0
* **Target SDK**: Android 14 (API 34)
* **Minimum SDK**: Android 8.0 Oreo (API 26)
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
* **Concurrency**: Kotlin Coroutines & StateFlow
* **Persistence**: Jetpack DataStore Preferences
* **Telephony APIs**: Android Telecom Framework, `CallScreeningService`, `TelephonyManager`, `AudioManager`
* **Architecture**: Clean MVVM / Unidirectional Data Flow

---

## 🏗️ Building from Source

To build the APK from source, clone the repository and run:

```bash
# Clone the repository
git clone https://github.com/anesbo/khoshoo3.git
cd khoshoo3

# Build debug APK
./gradlew assembleDebug

# Build release APK (v1.0.0)
./gradlew assembleRelease
```

The output release APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 👤 Author & Copyright

**Created and Developed by Anes Bouziad**

* **GitHub**: [@anesbo](https://github.com/anesbo)
* **Instagram**: [@anesb0](https://www.instagram.com/anesb0?obrf=MTh0eW1qNTAwdDFmbA==)

```
Copyright © 2026 Anes Bouziad. All Rights Reserved.
Khushoo' (خشوع) is distributed for personal focus and peace of mind.
```
