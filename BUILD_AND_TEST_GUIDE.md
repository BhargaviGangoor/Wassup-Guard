# Build & Test Guide - Wassup Guard

## ✅ **Verification Results**

### **Status Check:**
- ✅ **API Key**: Found and configured
- ✅ **Gradle Wrapper**: Present
- ✅ **Key Files**: All present
- ⚠️ **Java/JDK**: Not in PATH (use Android Studio)
- ⚠️ **Android SDK**: Path configured

---

## 🚀 **Recommended: Build via Android Studio**

### **Why Android Studio?**
- ✅ Handles Java/JDK automatically
- ✅ Easy Gradle sync
- ✅ Built-in testing tools
- ✅ Logcat for debugging
- ✅ Database Inspector
- ✅ No command line setup needed

---

## 📋 **Step-by-Step Build Instructions**

### **Step 1: Open Project**
1. Launch **Android Studio**
2. Click **"Open"** or **File → Open**
3. Navigate to: `C:\Users\abgan\Wassup-Guard`
4. Click **"OK"**
5. Wait for project to load (may take a few minutes first time)

### **Step 2: Sync Gradle**
1. Android Studio will show: **"Gradle files have changed"**
2. Click **"Sync Now"** button
3. OR: **File → Sync Project with Gradle Files**
4. Wait for sync to complete
5. Check bottom status bar: **"Gradle sync completed"**

**What happens during sync:**
- Downloads dependencies
- Generates BuildConfig (with your API key)
- Validates project structure
- Sets up build configuration

### **Step 3: Build Project**
1. **Build → Make Project** (or press `Ctrl+F9`)
2. Wait for build to complete
3. Check **"Build"** tab at bottom for errors
4. Should see: **"BUILD SUCCESSFUL"**

### **Step 4: Run App**
1. **Connect Android device** OR **Start emulator**
   - Device: Enable USB debugging
   - Emulator: Tools → Device Manager → Create/Start
2. Click green **"Run"** button (▶️) in toolbar
3. OR: **Run → Run 'app'**
4. Select device/emulator
5. App will install and launch

---

## 🧪 **Testing the App**

### **1. Check Logcat**
1. Open **Logcat** tab at bottom
2. Filter by: **"FileMonitorWorker"**
3. Should see:
   - `"Worker started - scanning WhatsApp media folders"`
   - `"Scanning file: ..."`
   - `"File hash: ..."`
   - `"API request recorded - Daily: X/500, Monthly: Y/15500"`

### **2. Test File Scanning**
1. Add a test PDF/image to WhatsApp folder
2. OR: Add to app's test_files folder
3. Wait for scan to complete
4. Check Logcat for scan results

### **3. Check Notifications**
1. Should receive notification after scan
2. Title: "Scan Complete" or "⚠️ Threat Detected!"
3. Check notification settings if not received

### **4. Verify Database**
1. **View → Tool Windows → App Inspection**
2. Click **"Database Inspector"**
3. Look for: **"wassupguard_database"**
4. Check tables:
   - `scan_logs` - Should have scan records
   - `signatures` - Should have threat signatures (if any found)

### **5. Test Rate Limiting**
1. Check Logcat for rate limiting messages
2. Should see: `"API request recorded - Daily: X/500"`
3. After 4 requests, should see 15-second delays

---

## ✅ **Build Verification Checklist**

After building, verify:

- [ ] **Build successful** (no errors)
- [ ] **BuildConfig generated** (contains API key)
- [ ] **App installs** on device/emulator
- [ ] **Permissions granted** (notifications, storage)
- [ ] **Worker runs** automatically (check Logcat)
- [ ] **Files scanned** (check Logcat)
- [ ] **Notifications received**
- [ ] **Database created** (use Database Inspector)
- [ ] **No crashes** or errors

---

## 🐛 **Troubleshooting**

### **Issue: "Gradle sync failed"**
**Solutions:**
1. Check internet connection (needs to download dependencies)
2. **File → Invalidate Caches / Restart**
3. Try: **File → Sync Project with Gradle Files** again
4. Check if Android SDK is properly installed

### **Issue: "BuildConfig.VIRUSTOTAL_API_KEY not found"**
**Solutions:**
1. Verify `local.properties` has API key
2. **File → Sync Project with Gradle Files**
3. **Build → Clean Project**
4. **Build → Rebuild Project**

### **Issue: "SDK location not found"**
**Solutions:**
1. Check `local.properties` has correct SDK path
2. Update path if needed
3. Re-sync Gradle

### **Issue: "No device found"**
**Solutions:**
1. Enable USB debugging on device
2. OR: Start Android emulator
3. Check device appears in device dropdown

### **Issue: "App crashes on launch"**
**Solutions:**
1. Check Logcat for error messages
2. Verify all permissions granted
3. Check if database initialized correctly

---

## 📊 **Expected Behavior**

### **On App Launch:**
1. App opens to main screen
2. Notification permission requested (Android 13+)
3. Worker scheduled automatically
4. Background scan starts

### **During Scan:**
1. Files found in WhatsApp folders
2. Hashes generated for each file
3. Database checked first
4. API called for unknown files (with rate limiting)
5. Results saved to database
6. Notifications sent

### **After Scan:**
1. Notification received
2. Scan logs saved to database
3. Threat signatures cached
4. Quarantined files moved (if malicious)

---

## 🎯 **Quick Test Commands (If Java Configured)**

If you set up Java/JDK, you can use:

```powershell
# Navigate to project
cd C:\Users\abgan\Wassup-Guard

# Sync dependencies
.\gradlew.bat --refresh-dependencies

# Build debug APK
.\gradlew.bat assembleDebug

# Install on connected device
.\gradlew.bat installDebug

# Run unit tests
.\gradlew.bat test
```

**Note**: These require Java/JDK in PATH. Android Studio handles this automatically.

---

## 📝 **Next Steps After Building**

1. **Test with real files** - Add PDF/image to WhatsApp
2. **Monitor usage** - Check rate limiting is working
3. **Check database** - Verify scan logs are saved
4. **Test quarantine** - Test with known malicious file (if available)
5. **Frontend development** - Start building UI

---

## ✅ **Summary**

**Status**: ✅ **Ready to Build**

**Recommended Method**: **Android Studio** (easiest and most reliable)

**Steps**:
1. Open in Android Studio
2. Sync Gradle
3. Build project
4. Run app
5. Test functionality

**All files are ready!** Just open in Android Studio and sync! 🚀

---

**Created**: $(date)
**Status**: ✅ **READY**

