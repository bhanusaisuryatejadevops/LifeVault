# Google Play Console Data Safety Questionnaire Guide

In Google Play Console, go to:
**Policy and programs > App content > Data safety**

Follow this exact question-by-question guide based on verified code inspection of LifeVault v1.0 and the integrated Google Mobile Ads SDK (AdMob).

---

## Section 1: Data Collection & Security Overview

| Play Console Question | Verified Answer | Explanation / Notes |
|---|---|---|
| **Does your app collect or share any of the required user data types?** | **Yes** | Even though LifeVault's core app saves everything locally, Google Play rules mandate declaring data processed by bundled third-party advertising SDKs (Google Mobile Ads SDK). |
| **Is all of the user data collected by your app encrypted in transit?** | **Yes** | All network communication performed by Google Mobile Ads SDK uses HTTPS/TLS. |
| **Do you provide a way for users to request that their data be deleted?** | **Yes** | LifeVault allows deleting records at any time in-app and clearing all data via Android system settings. |

---

## Section 2: Data Types Breakdown

Review each category and select as follows:

### 1. Location
- **Approximate location (Coarse, non-precise):**
  - Check **Collected** (Processed by Google AdMob via IP address for ad delivery and regional fraud prevention).
  - Check **Not Shared**.
  - **Is this data processed ephemerally?** Select **Yes**.
  - **Is this data required or optional?** Select **Data collection is required** (required by AdMob to deliver ads).
  - **Why is this user data collected?** Select **Advertising or marketing** and **Fraud prevention, security, and compliance**.

### 2. Personal info
- Name, Email, Address, Phone, Race/Ethnicity, Political/Religious beliefs, Sexual orientation, Other personal info:
  - **None collected.** (All checkboxes remain UNCHECKED).

### 3. Financial info
- Credit card, debit card, bank account numbers, purchase history, credit score:
  - **None collected.** (All purchase prices entered into LifeVault remain solely in your local on-device SQLite database and are NEVER transmitted).

### 4. Health and fitness
- **None collected.**

### 5. Messages
- **None collected.**

### 6. Photos and videos
- **None collected.** (Receipt photos are stored solely in your private internal storage sandbox `context.filesDir/receipts/` and are never collected or transmitted by LifeVault).

### 7. Audio files
- **None collected.**

### 8. Files and docs
- **None collected.**

### 9. Calendar
- **None collected.**

### 10. Contacts
- **None collected.**

### 11. App activity
- Page views, app interactions, in-app search history, installed apps:
  - Check **Collected** under **App interactions** (AdMob tracks ad impressions/clicks).
  - **Is this data processed ephemerally?** Select **No** (aggregated reporting).
  - **Why is this user data collected?** Select **Advertising or marketing** and **Analytics**.

### 12. Web browsing
- **None collected.**

### 13. App info and performance
- **Crash logs / Diagnostics:**
  - Check **Collected** under **Crash logs** and **Diagnostics**.
  - **Is this data processed ephemerally?** Select **Yes**.
  - **Why is this user data collected?** Select **Analytics** and **Fraud prevention, security, and compliance**.

### 14. Device or other IDs
- **Device or other IDs (e.g., Google Advertising ID / AAID):**
  - Check **Collected**.
  - Check **Not Shared**.
  - **Is this data processed ephemerally?** Select **No** (AdMob uses Advertising ID for frequency capping and attribution).
  - **Why is this user data collected?** Select **Advertising or marketing** and **Fraud prevention, security, and compliance**.

---

## Section 3: Summary Display to Store Visitors

Once submitted, Google Play will display a clear, trusted summary on your store listing:

1. **No data shared with third parties** (No broker selling or personal data sharing).
2. **Data collected:** Device or other IDs, App activity, and Diagnostics for Advertising and Analytics.
3. **Security practices:** Data is encrypted in transit using HTTPS.
4. **Data can be deleted:** Users can delete their data at any time.
