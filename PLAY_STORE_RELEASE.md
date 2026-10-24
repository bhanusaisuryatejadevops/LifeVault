# LifeVault — Google Play Store Release & Deployment Guide

## 1. Google Play Store Listing Metadata

### App Title (30 characters max - Google Play Policy compliant)
```
LifeVault: Private Organizer
```
*(28 characters — contains no emojis, ALL CAPS, or buzzwords like "Free"/"#1")*

### Short Description (80 characters max)
```
Securely track product warranties, receipts, return windows, and life deadlines.
```
*(80 characters)*

### Full Description
```
LifeVault is your private, offline personal life organizer designed to keep you on top of product warranties, purchase receipts, return deadlines, document expiration dates, and important personal reminders—with 100% on-device privacy.

Never lose money on an expired return window or miss a manufacturer warranty claim again.

KEY FEATURES:

🛡️ 100% Private & Offline by Design
Your financial data, purchase receipts, serial numbers, and personal notes belong to you. LifeVault operates entirely on your device with no required cloud accounts, no user logins, and zero telemetry tracking your personal items.

📷 On-Device Receipt Scanner & OCR
Capture paper receipts using your phone's camera or photo library. LifeVault processes receipt images directly on your device using on-device machine learning text recognition (ML Kit) to detect merchant names, purchase totals, and dates. You always verify and confirm extracted fields before saving.

⏰ Smart Deadline Alerts & Reminders
Get timely, customizable notifications before:
• Return windows close (default 2 days before)
• Manufacturer and extended warranties expire (default 14 days before)
• Passports, driver's licenses, and IDs expire (default 30 days before)
• Custom maintenance dates or service renewals occur

📊 Comprehensive Dashboard & Metrics
At a glance, monitor:
• Active warranty coverage
• Urgent closing return windows
• Upcoming expirations and overdue items
• Total protected asset valuation
• Categorized breakdown across Electronics, Appliances, Home, Vehicles, Documents, and more

📅 Chronological Timeline & Calendar View
Visualize all your upcoming deadlines in an intuitive timeline so you can plan service requests, returns, and renewals well ahead of time.

🔐 Encrypted Local Backups
Export your complete records into an AES-256-GCM encrypted backup file secured by your personal master password. Keep your backup on your device or transfer it securely under your own terms.
```

---

## 2. Google Play Console Data Safety Questionnaire

| Question | Answer | Details / Explanation |
|---|---|---|
| **Does your app collect or share any user data?** | **Yes** | Solely third-party diagnostic & advertising data processed by the Google Mobile Ads SDK (AdMob). The core app collects 0 bytes of user data. |
| **Is all user data collected by your app encrypted in transit?** | **Yes** | All SDK network traffic uses HTTPS/TLS. |
| **Do you provide a way for users to request that their data be deleted?** | **Yes** | Users have total control to delete any individual records at any time directly in the app, or clear all app data via Android Settings > Apps > LifeVault > Clear Storage. |
| **Personal Info (Name, Email, Address, Phone)** | **Not collected** | No accounts or cloud databases are used. |
| **Financial Info (Receipts, Credit Cards, Purchase History)** | **Not collected** | All receipt photos, purchase prices, and store names remain on the physical device. |
| **Photos and Videos** | **Not collected** | Receipt photos are stored exclusively in the app's internal sandboxed storage. |
| **App Activity / Analytics** | **Not collected** | No analytics SDK (Firebase Analytics, Mixpanel, etc.) is integrated. |
| **Device or other IDs** | **Collected (Ephemeral)** | Processed by Google Mobile Ads SDK for ad serving, frequency capping, and fraud prevention in accordance with Google AdMob policy. |

---

## 3. Google AdMob Configuration

### Test vs. Production Ad Units
During development and verification, LifeVault uses Google's official AdMob test ad unit IDs:
- **Test App ID:** `ca-app-pub-3940256099942544~3347511713` (configured in `AndroidManifest.xml`)
- **Test Banner Unit ID:** `ca-app-pub-3940256099942544/6300978111`
- **Test Interstitial Unit ID:** `ca-app-pub-3940256099942544/1033173712`

### Switching to Production for Play Store Release:
1. In your Google AdMob Console, create an Android App and generate:
   - Your Production AdMob Application ID
   - A Production Adaptive Banner Ad Unit ID
   - A Production Interstitial Ad Unit ID
2. In `app/src/main/AndroidManifest.xml`, update the meta-data value:
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY" />
   ```
3. Update `TEST_BANNER_ID` and `TEST_INTERSTITIAL_ID` in `AdMobManager.kt` or inject them dynamically via `SharedPreferences` or `BuildConfig`.
4. LifeVault incorporates strict monetization compliance:
   - **No accidental clicks:** Banners are placed in non-interactive bottom navigation areas.
   - **Frequency capping:** Interstitials are capped to at least 4 minutes between impressions and require at least 3 natural screen transitions.
   - **Workflow integrity:** Interstitials never interrupt data entry, receipt photography, OCR scanning, or alarm saves.
   - **Offline resilience:** If offline or ad load fails, banner views gracefully collapse with zero layout displacement.

---

## 4. Building the Signed Android App Bundle (.aab)

To generate the final Android App Bundle for upload to the Google Play Console:

```bash
# 1. Ensure clean build & compile
gradle :app:assembleRelease

# 2. Build the production App Bundle (.aab)
gradle :app:bundleRelease
```

The resulting bundle will be generated at:
`app/build/outputs/bundle/release/app-release.aab`
