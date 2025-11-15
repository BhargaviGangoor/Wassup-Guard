# Build & Test Instructions

## ⚠️ **Java/JDK Required**

To build the Android app, you need:
1. **Java JDK 11 or higher** (required by the project)
2. **Android SDK** (already configured in `local.properties`)

---

## 🔧 **Option 1: Build via Android Studio (Recommended)**

### **Steps:**

1. **Open Project in Android Studio**
   - Open Android Studio
   - File → Open → Select `Wassup-Guard` folder
   - Wait for project to load

2. **Sync Gradle Project**
   - Android Studio will automatically prompt: "Gradle files have changed"
   - Click **"Sync Now"** button
   - OR: File → Sync Project with Gradle Files
   - Wait for sync to complete (check bottom status bar)

3. **Build Project**
   - Build → Make Project (Ctrl+F9)
   - OR: Build → Rebuild Project
   - Wait for build to complete
   - Check "Build" tab at bottom for errors

4. **Run App**
   - Connect Android device or start emulator
   - Click green "Run" button (▶️)
   - OR: Run → Run 'app'
   - App will install and launch

---

## 🔧 **Option 2: Build via Command Line**

### **Prerequisites:**
- Java JDK 11+ installed
- JAVA_HOME environment variable set
- Android SDK configured

### **Steps:**

1. **Set JAVA_HOME** (if not set):
   ```powershell
   # Find Java installation
   # Usually in: C:\Program Files\Java\jdk-11 or similar
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-11"
   ```

2. **Sync Gradle:**
   ```powershell
   cd C:\Users\abgan\Wassup-Guard
   .\gradlew.bat --refresh-dependencies
   ```

3. **Build APK:**
   ```powershell
   .\gradlew.bat assembleDebug
   ```

4. **Build and Install:**
   ```powershell
   .\gradlew.bat installDebug
   ```

5. **Run Tests:**
   ```powershell
   .\gradlew.bat test
   ```

---

## ✅ **What to Check After Build**

### **1. Build Success**
- ✅ No compilation errors
- ✅ No missing dependencies
- ✅ BuildConfig generated (contains API key)

### **2. Verify API Key**
- Check `app/build/generated/source/buildConfig/debug/com/example/wassupguard/BuildConfig.java`
- Should contain: `public static final String VIRUSTOTAL_API_KEY = "your-key";`

### **3. Check Logs**
- Open Logcat in Android Studio
- Filter by "FileMonitorWorker"
- Should see scanning activity

### **4. Test Functionality**
- ✅ App installs successfully
- ✅ Permissions granted
- ✅ Worker runs automatically
- ✅ Notifications received
- ✅ Database created

---

## 🐛 **Common Issues**

### **Issue 1: "JAVA_HOME is not set"**
**Solution:**
- Install JDK 11 or higher
- Set JAVA_HOME environment variable
- Restart terminal/Android Studio

### **Issue 2: "BuildConfig.VIRUSTOTAL_API_KEY not found"**
**Solution:**
- Sync Gradle project
- Rebuild project
- Check `local.properties` has API key

### **Issue 3: "SDK location not found"**
**Solution:**
- Check `local.properties` has correct `sdk.dir` path
- Update path if needed

### **Issue 4: "Gradle sync failed"**
**Solution:**
- Check internet connection (downloads dependencies)
- File → Invalidate Caches / Restart
- Try again

---

## 📱 **Testing Checklist**

After building and running:

- [ ] App installs on device/emulator
- [ ] Permissions requested and granted
- [ ] Main screen appears
- [ ] Worker runs automatically (check Logcat)
- [ ] Files are scanned (check Logcat)
- [ ] Notifications received
- [ ] Database created (use Database Inspector)
- [ ] Rate limiting works (check logs)
- [ ] No crashes or errors

---

## 🎯 **Quick Test Commands**

### **Check if Gradle works:**
```powershell
cd C:\Users\abgan\Wassup-Guard
.\gradlew.bat --version
```

### **Check dependencies:**
```powershell
.\gradlew.bat dependencies
```

### **Clean build:**
```powershell
.\gradlew.bat clean
.\gradlew.bat build
```

---

## 💡 **Recommended Approach**

**Use Android Studio** - It's the easiest way:
1. Opens project
2. Automatically syncs Gradle
3. Shows build errors clearly
4. Easy to run and debug
5. Built-in Logcat for testing

---

**Status**: Ready to build!
**Next Step**: Open in Android Studio and sync Gradle

