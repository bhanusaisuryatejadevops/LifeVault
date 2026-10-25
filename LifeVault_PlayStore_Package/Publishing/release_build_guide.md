# Android Studio Release Build & Keystore Guide (Windows 10)

This guide walks you through generating a production-ready, signed **Android App Bundle (.aab)** using Android Studio on Windows 10.

---

## 1. Important Application Identification & Versioning

Open `app/build.gradle.kts` in Android Studio and check lines 16–23:

```kotlin
defaultConfig {
    applicationId = "com.aistudio.lifevault.qxzkrm"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
}
```

### Critical Rules:
1. **Application ID (`applicationId`):**
   - This is the unique permanent identifier of your app on Google Play.
   - **DO NOT change this ID after uploading to Google Play.** Changing it creates an entirely different app listing and orphans existing users.
2. **Versioning Rules for Updates:**
   - Every time you publish an update to Google Play, increment `versionCode` by 1 (e.g., from `1` to `2`). Google Play rejects bundles with duplicate version codes.
   - Update `versionName` to describe the release to users (e.g., `"1.0.1"` or `"1.1"`).

---

## 2. Production AdMob Configuration (Before Release)

During testing, LifeVault uses official Google test ads to protect your account against invalid click suspensions. When you are ready to earn real revenue:

1. Log in to [Google AdMob](https://admob.google.com).
2. Click **Apps > Add App > Android > Yes, it's listed** (or "No" if not published yet). Name it **LifeVault**.
3. Create two Ad Units:
   - **Banner Ad Unit:** Click *Add Ad Unit > Banner*. Copy your Ad Unit ID (`ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY`).
   - **Interstitial Ad Unit:** Click *Add Ad Unit > Interstitial*. Copy your Ad Unit ID.
4. Open `app/src/main/AndroidManifest.xml` in Android Studio and replace the test application ID:
   ```xml
   <!-- Replace the test ID below with your actual AdMob App ID -->
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-YOUR_ADMOB_ACCOUNT~YOUR_APP_ID" />
   ```
5. Open `app/src/main/java/com/example/monetization/AdMobManager.kt`:
   - Replace `TEST_BANNER_ID` and `TEST_INTERSTITIAL_ID` with your production ad unit strings.

---

## 3. Creating Your Secure Upload Keystore in Android Studio

Google Play requires all App Bundles to be signed with a digital cryptographic key (.jks).

### Step-by-Step Creation on Windows:

1. Open your **LifeVault** project in Android Studio.
2. On the top menu bar, click: **Build > Generate Signed Bundle / APK...**
3. Select **Android App Bundle** and click **Next**.
4. Under **Key store path**, click **Create new...**
5. In the **New Key Store** window:
   - **Key store path:** Click the folder icon, choose a safe folder on your PC (e.g., `C:\Users\YOUR_USERNAME\Documents\AndroidKeystores\lifevault-upload-key.jks`). *Do NOT save it inside a public git repo!*
   - **Password:** Create a strong password (minimum 8 characters) and confirm it.
   - **Alias:** Enter `lifevault_upload` (or leave as `key0`).
   - **Key Password:** Enter a strong password for the key alias.
   - **Validity (years):** Set to `25` (standard default).
   - **First and Last Name:** Enter your name or company name.
   - Other certificate fields (Organization, City, Country) are optional.
6. Click **OK**.
7. **BACK UP YOUR KEYSTORE NOW:**
   - Copy `lifevault-upload-key.jks` and your passwords to a secure password manager or encrypted USB drive.
   - Google Play App Signing can help recover keys, but keeping your primary keystore safe is vital.

---

## 4. Generating the Signed Android App Bundle (.aab)

1. In the **Generate Signed Bundle or APK** window:
   - Verify the **Key store path**, **Key store password**, **Key alias**, and **Key password** are filled.
   - Check **Remember passwords** if you want Android Studio to remember them on this PC.
   - Click **Next**.
2. Select Destination:
   - **Build Variants:** Choose `release`.
3. Click **Create** (or **Finish**).
4. Android Studio will execute the Gradle build. After 1–2 minutes, a notification appears on the bottom right:
   > **"Generate Signed Bundle: Successfully generated signed bundle for module 'app' with build variant 'release'."**
5. Click **Locate** inside the popup.
   - Windows Explorer will open the folder containing:
     `app-release.aab`
   - *Typical path:* `app\release\app-release.aab`
6. This `.aab` file is the exact file you upload to Google Play Console under **Production > Create new release > Upload**.
