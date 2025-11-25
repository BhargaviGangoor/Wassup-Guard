# Wassup Guard - Beginner's Guide to Android Development

## 🎯 What This App Does

**Wassup Guard** is a security app that:
- Scans WhatsApp media files (PDFs and images) for malware
- Checks files against VirusTotal's threat database
- Quarantines dangerous files automatically
- Shows you a safety score for each file
- Works in the background without bothering you

## 📚 Android Development Concepts Explained Simply

### 1. **Application Class** (`WassupGuardApplication.kt`)
**What it is**: A class that runs when your app starts (before any screen appears)

**Why we need it**: 
- Sets up things that the whole app needs (like the database)
- Runs once when app launches
- Like a "setup" function

**Real-world analogy**: Like turning on a computer - it does initial setup before you can use it

---

### 2. **WorkManager** (Background Tasks)
**What it is**: Android's way to run tasks in the background, even when app is closed

**Why we need it**:
- Scans need to run automatically
- Can't block the main app (would freeze UI)
- Works even if user closes the app

**Real-world analogy**: Like a scheduled task on your computer that runs automatically

**In our app**: `FileMonitorWorker` runs scans in the background

---

### 3. **Room Database** (Local Storage)
**What it is**: Android's database library - stores data on the device

**Why we need it**:
- Store scan results
- Cache threat signatures (avoid repeated API calls)
- Fast lookups

**Real-world analogy**: Like a filing cabinet where you store important documents

**In our app**: 
- `ScanLog` - stores scan history
- `Signature` - stores known threat hashes

---

### 4. **Retrofit** (API Calls)
**What it is**: Library that makes it easy to talk to web APIs

**Why we need it**:
- Communicate with VirusTotal API
- Handles all the HTTP complexity
- Converts JSON responses to Kotlin objects

**Real-world analogy**: Like a translator that converts your requests into web language

**In our app**: `ApiClient` and `VirusTotalApi` handle API communication

---

### 5. **Coroutines** (Async Operations)
**What it is**: Kotlin's way to do multiple things at once without blocking

**Why we need it**:
- Database and network calls take time
- Can't freeze the UI while waiting
- Like JavaScript promises or async/await

**Real-world analogy**: Like multitasking - you can do other things while waiting

**In our app**: All database and API calls use `suspend` functions

---

### 6. **FileObserver** (File Monitoring)
**What it is**: Watches a folder for new files, deletions, changes

**Why we need it**:
- Detect new WhatsApp downloads immediately
- Trigger scans automatically
- Real-time monitoring

**Real-world analogy**: Like a security camera watching a folder

**In our app**: `WhatsAppFileObserver` watches WhatsApp media folders

---

### 7. **BuildConfig** (Configuration)
**What it is**: A way to store configuration values (like API keys) securely

**Why we need it**:
- API keys shouldn't be in source code
- Different values for different environments
- Read from `local.properties` (gitignored)

**Real-world analogy**: Like a settings file that's not shared publicly

**In our app**: VirusTotal API key is stored in `local.properties`

---

## 🔄 How the App Works (Step by Step)

### Step 1: App Starts
```
User opens app
    ↓
WassupGuardApplication.onCreate() runs
    ↓
Database is initialized (lazy - only when needed)
    ↓
MainActivity shows the UI
```

### Step 2: Background Scan Starts
```
WorkManager schedules FileMonitorWorker
    ↓
Worker runs in background
    ↓
Scans WhatsApp media folders
```

### Step 3: File Scanning Process
```
Find PDF/image file
    ↓
Generate SHA-256 hash (like a fingerprint)
    ↓
Check local database first (fast!)
    ↓
If not found → Query VirusTotal API
    ↓
Calculate safety score (0-100)
    ↓
If malicious → Quarantine file
    ↓
Save scan result to database
    ↓
Send notification to user
```

### Step 4: Real-time Monitoring
```
WhatsAppFileObserver watches folders
    ↓
New file detected
    ↓
Trigger scan immediately
    ↓
Same process as Step 3
```

## 📁 Key Files Explained

### `FileMonitorWorker.kt`
**What**: The main scanning engine
**Does**: 
- Scans files
- Generates hashes
- Checks VirusTotal
- Quarantines threats
- Saves results

### `QuarantineManager.kt`
**What**: Safety vault for dangerous files
**Does**: 
- Moves malicious files to secure folder
- Prevents access
- Keeps records

### `SafeScoreCalculator.kt`
**What**: Calculates how safe a file is
**Does**: 
- Analyzes VirusTotal results
- Gives 0-100 score
- Provides safety label

### `WhatsAppFileObserver.kt`
**What**: Watches WhatsApp folders
**Does**: 
- Monitors for new files
- Filters PDF/images only
- Triggers scans

### `ApiClient.kt`
**What**: Sets up VirusTotal API connection
**Does**: 
- Configures HTTP client
- Adds API key to requests
- Creates Retrofit instance

## 🛠️ Setup Instructions

### 1. Get VirusTotal API Key
1. Go to https://www.virustotal.com/
2. Sign up (free account)
3. Go to API section
4. Copy your API key

### 2. Add API Key to Project
1. Open `local.properties` (in project root)
2. Add this line:
   ```
   VIRUSTOTAL_API_KEY=your-key-here
   ```
3. Replace `your-key-here` with your actual key

### 3. Sync Gradle
1. In Android Studio: File → Sync Project with Gradle Files
2. Wait for sync to complete

### 4. Run the App
1. Click Run button (green play icon)
2. App will install on device/emulator
3. Grant permissions when asked

## 🧪 Testing

### Test the Backend:
1. **Check Logs**: 
   - Open Logcat in Android Studio
   - Filter by "FileMonitorWorker"
   - Should see scanning activity

2. **Check Database**:
   - Tools → App Inspection → Database Inspector
   - Look for `wassupguard_database`
   - Check `scan_logs` table

3. **Test with Files**:
   - Add a test PDF/image to WhatsApp
   - Wait for scan to complete
   - Check notification

## 🐛 Common Issues

### Issue: "BuildConfig.VIRUSTOTAL_API_KEY not found"
**Solution**: 
- Make sure you added API key to `local.properties`
- Sync Gradle project
- Rebuild project

### Issue: "No WhatsApp folders found"
**Solution**:
- App falls back to scanning app's test_files folder
- On real device, WhatsApp folders should be detected
- May need MediaStore API for Android 10+

### Issue: "API rate limit exceeded"
**Solution**:
- VirusTotal free tier: 4 requests/minute
- App caches results to minimize calls
- Wait a minute and try again

## 📖 Learning Resources

- **Android Developers**: https://developer.android.com/
- **Kotlin Docs**: https://kotlinlang.org/docs/home.html
- **Room Database**: https://developer.android.com/training/data-storage/room
- **WorkManager**: https://developer.android.com/topic/libraries/architecture/workmanager

## 🎓 Next Steps

1. **Understand the code**: Read through each file with comments
2. **Experiment**: Try changing values, see what happens
3. **Add features**: Start with simple UI improvements
4. **Learn Android**: Follow official Android tutorials
5. **Practice**: Build small projects to learn

## 💡 Tips for Beginners

1. **Read the comments**: Every file has detailed comments
2. **Use Logcat**: See what's happening in real-time
3. **Break things**: Don't be afraid to experiment
4. **Ask questions**: Android development has a great community
5. **Start small**: Understand one concept at a time

---

**Remember**: Every expert was once a beginner. Take your time, experiment, and have fun learning! 🚀

