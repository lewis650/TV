# Lewis Home: custom home screen for the Sony XR-85X90K

This is a custom Google TV home screen. It shows:

- A greeting in the centre: "Good morning, Lewis" before midday, "Good afternoon" until 5pm, and "Good evening" from 5pm.
- A large clock and the date below the greeting.
- A dock of **TOD · Netflix · Prime Video** along the bottom, plus an **All apps** tile.
- An all-apps grid and a Settings shortcut (top right).
- A sky background that changes with the time of day. You can swap in your own photos (step 5).

## 1. Build the app (one time)

**Easiest: GitHub, free, no software needed**

1. Create a free account at github.com, then create a new **private** repository.
2. Upload everything in this folder, including the hidden `.github` folder.
3. Open the **Actions** tab. A build runs automatically and takes about 3 minutes.
4. Open the finished run and download **lewis-home-apk**. Unzip it to get `app-debug.apk`.

**Or: Android Studio on any laptop**

Open this folder, then choose Build → Build APK(s).

## 2. Prepare the TV

1. On the TV, go to **Settings → System → About**.
2. Scroll to **Android TV OS build** and click it 7 times until it says you're a developer.
3. Go back to **Settings → System → Developer options** and turn on **USB debugging**.
4. Note the TV's IP address under **Settings → Network & Internet** (it looks like 192.168.1.x).

## 3. Install from a laptop on the same Wi‑Fi

1. Install Android "platform-tools", which includes `adb`:
   - Mac: `brew install android-platform-tools`
   - Windows: download it from developer.android.com
2. Run these commands:

```
adb connect 192.168.1.x:5555        # use your TV's IP; press "Allow" on the TV when asked
adb install app-debug.apk
```

"Lewis Home" now appears in the TV's apps list.

## 4. Make it the home screen

**Option A (recommended, safest): remap the Home button**

1. Install **Button Mapper** from the TV's Play Store.
2. Set the Home button to open **Lewis Home**.

Everything else on the TV stays exactly as it was.

**Option B: replace Google TV Home completely**

```
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith
```

Then press Home and choose **Lewis Home → Always**.

After doing this, check that the remote's **Input** button still switches HDMI sources. If it doesn't, undo with:

```
adb shell pm enable com.google.android.apps.tv.launcherx
adb shell pm enable com.google.android.tungsten.setupwraith
```

## 5. Personalise

- **Wallpaper:** add 1920×1080 photos to `app/src/main/res/drawable-nodpi/`. Name them `wallpaper_morning.jpg`, `wallpaper_afternoon.jpg`, `wallpaper_evening.jpg` and `wallpaper_night.jpg`, or use one file called `wallpaper.jpg`. Then rebuild.
- **Dock apps / name:** edit `FAVOURITES` and `NAME` at the top of `MainActivity.java`. Apps are matched by the name shown on the TV, e.g. "YouTube" or "Shahid".
- **Uninstall:** `adb uninstall com.allsopp.home`
