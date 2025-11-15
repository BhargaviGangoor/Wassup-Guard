# Build Verification Script for Wassup Guard
# This script checks if the project is ready to build

Write-Host "=== Wassup Guard Build Verification ===" -ForegroundColor Cyan
Write-Host ""

# Check 1: API Key
Write-Host "1. Checking API Key..." -ForegroundColor Yellow
$localProps = "local.properties"
if (Test-Path $localProps) {
    $content = Get-Content $localProps -Raw
    if ($content -match "VIRUSTOTAL_API_KEY=(.+)") {
        $apiKey = $matches[1].Trim()
        if ($apiKey.Length -gt 10) {
            Write-Host "   ✅ API Key found and configured" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  API Key appears to be empty or invalid" -ForegroundColor Red
        }
    } else {
        Write-Host "   ❌ API Key not found in local.properties" -ForegroundColor Red
    }
} else {
    Write-Host "   ❌ local.properties not found" -ForegroundColor Red
}

# Check 2: Gradle Wrapper
Write-Host ""
Write-Host "2. Checking Gradle Wrapper..." -ForegroundColor Yellow
if (Test-Path "gradlew.bat") {
    Write-Host "   ✅ Gradle wrapper found" -ForegroundColor Green
} else {
    Write-Host "   ❌ Gradle wrapper not found" -ForegroundColor Red
}

# Check 3: Android SDK
Write-Host ""
Write-Host "3. Checking Android SDK..." -ForegroundColor Yellow
if (Test-Path $localProps) {
    $content = Get-Content $localProps -Raw
    if ($content -match "sdk\.dir=(.+)") {
        $sdkPath = $matches[1].Trim() -replace '\\', '\'
        if (Test-Path $sdkPath) {
            Write-Host "   ✅ Android SDK found at: $sdkPath" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  Android SDK path configured but not found: $sdkPath" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ⚠️  Android SDK path not configured" -ForegroundColor Yellow
    }
}

# Check 4: Java/JDK
Write-Host ""
Write-Host "4. Checking Java/JDK..." -ForegroundColor Yellow
$javaHome = $env:JAVA_HOME
if ($javaHome -and (Test-Path $javaHome)) {
    Write-Host "   ✅ JAVA_HOME set: $javaHome" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  JAVA_HOME not set" -ForegroundColor Yellow
    Write-Host "   💡 Tip: Install JDK 11+ and set JAVA_HOME" -ForegroundColor Cyan
}

# Check 5: Key Files
Write-Host ""
Write-Host "5. Checking Key Files..." -ForegroundColor Yellow
$keyFiles = @(
    "app\build.gradle.kts",
    "app\src\main\AndroidManifest.xml",
    "app\src\main\java\com\example\wassupguard\WassupGuardApplication.kt",
    "app\src\main\java\com\example\wassupguard\workers\FileMonitorWorker.kt",
    "app\src\main\java\com\example\wassupguard\util\RateLimiter.kt"
)

$allFilesExist = $true
foreach ($file in $keyFiles) {
    if (Test-Path $file) {
        Write-Host "   ✅ $file" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $file (missing)" -ForegroundColor Red
        $allFilesExist = $false
    }
}

# Summary
Write-Host ""
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "To build the app:" -ForegroundColor Yellow
Write-Host "1. Open Android Studio" -ForegroundColor White
Write-Host "2. File → Open → Select this folder" -ForegroundColor White
Write-Host "3. File → Sync Project with Gradle Files" -ForegroundColor White
Write-Host "4. Build → Make Project" -ForegroundColor White
Write-Host "5. Run → Run 'app'" -ForegroundColor White
Write-Host ""
Write-Host "Or use command line (if Java is configured):" -ForegroundColor Yellow
Write-Host "  .\gradlew.bat assembleDebug" -ForegroundColor White
Write-Host ""

