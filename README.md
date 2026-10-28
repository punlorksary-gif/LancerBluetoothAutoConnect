# Lancer Bluetooth AutoConnect

An Android 14+ helper for a Samsung Galaxy S23 and a 2014 Mitsubishi Lancer whose paired Bluetooth entry is named **Hands Free System**. It monitors permitted Bluetooth connection events, identifies that paired device by name, reports the observable A2DP state, and presents a recovery action when the device connects.

## What it does

1. Requests `BLUETOOTH_CONNECT` at runtime so it can safely inspect bonded-device and profile state on Android 12+.
2. Requests notification permission on Android 13+.
3. Searches bonded devices for the exact (case-insensitive) name `Hands Free System`.
4. Watches the public `ACTION_ACL_CONNECTED` broadcast while the app is open, and declares a receiver for system delivery when it is available in the background.
5. On a matching connection, posts a high-priority notification. Tapping it opens Android Bluetooth settings, where **Media audio** can be turned on for the Lancer.
6. Shows whether Android reports an A2DP profile connection at that moment.

## Important limitation — no forced A2DP connection

This application deliberately does **not** call hidden APIs, reflection, shell commands, accessibility automation, or privileged system APIs. Android's public `BluetoothA2dp` API exposes status and a profile-proxy callback, but third-party apps are not given a public `connect()` method for a paired A2DP device. The platform (and, often, the car) owns that connection decision.

Consequently the app cannot guarantee that a 2014 Lancer begins media streaming, nor can it turn the device's “Media audio” toggle on by itself. The practical compliant recovery is its notification and direct Settings shortcut. On the phone, select **Hands Free System** and ensure **Media audio** is on; then choose **Bluetooth Audio** in the vehicle if required.

Some OEM releases can further limit background broadcasts or delay them when an app has been force-stopped. Open the app once after installation and do not force-stop it. The manual **Open Bluetooth settings** button always remains available.

## Build and install

Requirements: Android Studio Ladybug or newer, Android SDK Platform 35, JDK 17 (bundled with current Android Studio is suitable), and internet access for Gradle's first dependency download.

1. Unzip this project and open the `LancerBluetoothAutoConnect` folder in Android Studio.
2. Allow Gradle sync to finish and install an emulator/device SDK if Android Studio requests one.
3. Connect the Galaxy S23 with USB debugging enabled, or use a suitable Android 14+ emulator.
4. Choose the `app` configuration and press Run.
5. On first launch, grant Bluetooth and notification access. Pair the Lancer in the normal Android Bluetooth screen first.
6. Verify the app says `Paired device found: Hands Free System`. If your car advertises a different name, change `DEVICE_NAME` in `app/src/main/java/com/example/lancerbluetoothautoconnect/Recovery.kt`, rebuild, and reinstall.

For a signed release, use **Build → Generate Signed App Bundle / APK** in Android Studio. For a development APK, use **Build → Build APK(s)**.

## Test procedure

1. With the car off, open the app and tap **Refresh diagnostics**.
2. Start the car and wait for the phone to connect to Hands Free System.
3. The diagnostic screen updates while open; a matching ACL connection can also create a recovery notification.
4. Tap the notification, open the Lancer entry, and turn on **Media audio** if needed.
5. Confirm the screen reports an A2DP connection once media audio has connected.

## Project layout

- `MainActivity.kt` — permissions, screen, paired-device diagnostics, live event listener.
- `BluetoothEventReceiver.kt` — background ACL connection event receiver.
- `Recovery.kt` — device matching, A2DP-state readout, compliant notification recovery action.
