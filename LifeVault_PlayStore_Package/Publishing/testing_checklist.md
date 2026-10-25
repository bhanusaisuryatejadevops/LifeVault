# Pre-Launch Device QA & Regression Testing Checklist

Perform this manual testing checklist on your physical Android phone before pushing your release build to the public track.

---

## 1. First-Launch & Onboarding

- [ ] **Clean Install Launch:** The app opens directly to the Dashboard screen without flickering, placeholder cards, or crashes.
- [ ] **Initial Empty State:** Shows the "No Upcoming Deadlines" card with the "Add Your First Record" call to action.
- [ ] **Notification Permission (Android 13+):**
  - When prompted for notification permissions, granting permission displays notifications as scheduled.
  - Denying permission does not crash the app; records can still be saved and accessed normally.

---

## 2. Record Creation & Editing

- [ ] **Mandatory Field Validation:** Attempting to save a record with a blank item name shows the helpful error message "Item name is required" and does not save an empty record.
- [ ] **Date Pickers:**
  - Purchase Date defaults to today and can be set to past dates.
  - Warranty quick-selection chips (`+1 Year`, `+2 Years`, `+3 Years`, `Lifetime`) calculate exact future dates correctly.
  - Return Window quick chips (`+14 Days`, `+30 Days`, `+60 Days`) calculate deadlines accurately.
  - Document Expiry date picker works smoothly.
- [ ] **Receipt Photo Selection:**
  - Tapping "Attach Photo" opens the native Android Photo Picker.
  - Selecting a receipt image copies it to app-private internal storage and renders the thumbnail.
  - Removing an attached receipt via the 'X' button works cleanly.
- [ ] **On-Device Receipt OCR:**
  - Tapping "Run OCR" on an attached receipt triggers Google ML Kit on-device text recognition.
  - Extracted merchant names, prices, and dates appear as interactive chips in the `OcrConfirmSheet`.
  - Tapping candidate chips applies the verified values directly into the input fields.
  - Tapping "Discard" dismisses the sheet without altering manually entered text.
- [ ] **Record Saving:** Saving navigates directly to the new record's Detail view with a confirmation toast.

---

## 3. Local Persistence Across Device Restarts

- [ ] **App Task Killing:** Swipe LifeVault away from Android's Recent Apps list. Relaunch the app. All created items, photos, and prices remain intact.
- [ ] **Device Reboot:** Restart your physical Android phone. Relaunch LifeVault. All items remain in the SQLite database and AlarmManager reminders are rescheduled via `BootReceiver`.

---

## 4. Search, Filtering, and Sorting

- [ ] **Text Search:** Typing in the search bar instantly filters records by title, store name, notes, or serial number.
- [ ] **Category Chips:** Tapping a category chip (e.g. "Electronics") filters the list to items matching that category.
- [ ] **Status Chips:** Tapping "Return Window Closing" or "Active Coverage" displays only items matching that status.
- [ ] **Sort Order:** Switching sort order (Soonest Deadline, Purchase Date Newest/Oldest, Price Highest, Title A-Z) updates list ordering reactively.

---

## 5. Offline Capabilities & Edge Cases

- [ ] **Airplane Mode Test:**
  - Turn on Airplane Mode (no Wi-Fi, no mobile data).
  - Open LifeVault: All records, images, deadlines, and timeline functions work seamlessly.
  - Bottom banner ad gracefully collapses without leaving an empty white gap or causing layout shift.
  - No network timeout dialogs or crashes occur.
- [ ] **Large Dataset Performance:** Scroll smoothly through records without lag or memory leaks.

---

## 6. Backup & Restore (Encrypted)

- [ ] **Encrypted Export:**
  - In Settings, tap **Export Encrypted Backup**.
  - Enter a password (e.g. `MySecurePass123`). Tap **Export & Share**.
  - Copy the generated Base64 ciphertext or share to a text file.
- [ ] **Decrypted Import:**
  - Tap **Import Backup File**. Paste the payload and enter the password.
  - Records import successfully with a toast indicating the number of restored items.
  - Entering the wrong password fails safely with an error message and does not corrupt existing data.

---

## 7. Monetization & Ad Compliance

- [ ] **Non-Intrusive Banners:** Banners stay docked at the bottom of top-level screens and do not obscure floating action buttons or form inputs.
- [ ] **Natural Transition Interstitials:** Interstitials only appear upon exiting record details after meeting the 4-minute frequency cap. They never interrupt saving, photo capturing, or OCR scanning.
