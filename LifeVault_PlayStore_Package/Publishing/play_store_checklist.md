# Google Play Store Publishing Checklist

This checklist categorizes all requirements into **Mandatory (Hard Requirements)** that will cause rejection if omitted, and **Recommended Best Practices** for a successful launch.

---

## 1. Mandatory Requirements (Must Be Completed)

### Technical & Policy Checks
- [ ] **Target API Level:** LifeVault is configured with `targetSdk = 36` (exceeding Google Play's minimum requirement of API 34+).
- [ ] **Android App Bundle (.aab):** Google Play requires the `.aab` format (APK uploads are no longer accepted for new apps).
- [ ] **Unique Application ID:** `com.aistudio.lifevault.qxzkrm` is configured. *(Once published, this ID can never be changed).*
- [ ] **Valid HTTPS Privacy Policy:** Upload `privacy_policy.html` to GitHub Pages and link it in Play Console under **App content > Privacy policy**.
- [ ] **Data Safety Section:** Complete the questionnaire in Play Console following `data_safety_guide.md`.
- [ ] **Ads Declaration:**
  - In Play Console under **App content > Ads**, select: **"Yes, my app contains ads"**.
- [ ] **Target Audience & Content (COPPA):**
  - Under **App content > Target audience and content**:
  - Select target age: **18 and over** (or 13–17 and 18+).
  - Select **"No"** to: *Could your store listing unintentionally appeal to children?*
- [ ] **App Access (Testing Credentials):**
  - Under **App content > App access**:
  - Select: **"All functionality is available without special access"** (since LifeVault has no user login, phone authentication, or paywall).
- [ ] **Financial Features / Loan Declaration:**
  - LifeVault is an offline warranty/receipt tracker, NOT a loan/banking app. Select: *My app does not provide financial features or loans.*
- [ ] **Content Rating (IARC):**
  - Complete the IARC questionnaire following `content_rating_guide.md` to obtain an **Everyone / PEGI 3** rating.
- [ ] **Government Apps Declaration:**
  - Select: **"No"** (Not a government agency app).
- [ ] **Physical Address for Ads:**
  - In Play Console account settings, ensure a physical contact address is populated (required for ad-supported apps).

---

## 2. Store Presence & Listing Assets (Mandatory for Store)

- [ ] **App Name:** `LifeVault: Private Organizer` (≤ 30 characters).
- [ ] **Short Description:** `Track product warranties, purchase receipts, return deadlines & document expiry.` (≤ 80 characters).
- [ ] **Full Description:** Copy from `Store_Listing/full_description.txt` (≤ 4000 characters).
- [ ] **App Icon:** Upload `Branding/play_store_icon_512.png` (512 × 512 px PNG, < 1 MB).
- [ ] **Feature Graphic:** Upload `Branding/feature_graphic_1024x500.png` (1024 × 500 px PNG, < 15 MB).
- [ ] **Screenshots:** Upload at least 2 (recommended 4–6) portrait screenshots from your phone to **Phone screenshots**.

---

## 3. Recommended Best Practices (For Long-Term Success)

- [ ] **Replace Test Ad Unit IDs:** Before releasing to real public users, replace Google test ad unit IDs with your approved production AdMob unit IDs (documented in `release_build_guide.md`).
- [ ] **Backup Keystore Securely:** Store your release `.jks` file and passwords in at least two secure locations (e.g., password manager, encrypted USB). If you lose your upload key and do not use Google Play App Signing, you cannot update the app!
- [ ] **Internal / Closed Testing Track First:** Always release the first AAB to an **Internal testing** track or **Closed testing** track (20 testers for 14 days if using a personal developer account created after Nov 2023) before pushing directly to Production.
- [ ] **Review Release Notes:** Add clear user release notes:
  - *Example:* "Initial public release of LifeVault: 100% on-device warranty, receipt, and deadline organizer."
