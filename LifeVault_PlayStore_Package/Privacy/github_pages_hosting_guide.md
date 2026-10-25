# Free HTTPS Privacy Policy Hosting via GitHub Pages

Google Play requires a publicly accessible, valid HTTPS URL pointing to your app's Privacy Policy. **GitHub Pages** provides 100% free, reliable, permanent HTTPS hosting in 3 simple steps without paying for hosting or domains.

---

## Step 1: Create a Free GitHub Repository

1. Log in to [GitHub.com](https://github.com) (create a free account if you don't have one).
2. In the top-right corner, click **+** and select **New repository**.
3. Name your repository (e.g., `lifevault-privacy` or `privacy-policies`).
4. Set visibility to **Public** (required for free GitHub Pages).
5. Check **Add a README file**.
6. Click **Create repository**.

---

## Step 2: Upload `privacy_policy.html`

1. Inside your new repository, click **Add file > Upload files**.
2. Drag and drop the `privacy_policy.html` file from this package (`LifeVault_PlayStore_Package/Privacy/privacy_policy.html`).
   - *Tip:* Rename the file to `index.html` if you want a clean root URL like `https://yourusername.github.io/lifevault-privacy/`.
3. In the text box at the bottom, type: `Add LifeVault privacy policy`.
4. Click **Commit changes**.

---

## Step 3: Enable GitHub Pages

1. In your repository, click the **Settings** tab (the gear icon on the top right).
2. On the left sidebar menu under the "Code and automation" section, click **Pages**.
3. Under **Build and deployment > Source**, ensure **Deploy from a branch** is selected.
4. Under **Branch**, select `main` (or `master`), select folder `/ (root)`, and click **Save**.
5. Wait 60 to 90 seconds. Refresh the page.
6. A green box will appear:
   > **"Your site is live at https://YOUR_USERNAME.github.io/lifevault-privacy/"**

---

## Step 4: Paste URL into Google Play Console

1. Copy your live HTTPS link (e.g. `https://YOUR_USERNAME.github.io/lifevault-privacy/` or `.../privacy_policy.html`).
2. Open [Google Play Console](https://play.google.com/console).
3. Select **LifeVault** > **Policy and programs > App content > Privacy policy**.
4. Paste the URL into the **Privacy policy URL** field and click **Save**.

---

## Alternative Free Hosting Options (If you don't use GitHub)

- **Google Sites (sites.google.com):** Create a blank free Google Site, paste the text of the privacy policy, publish it publicly, and use that URL.
- **GitLab Pages / Netlify / Vercel:** Drop the `privacy_policy.html` file into a free static project.
