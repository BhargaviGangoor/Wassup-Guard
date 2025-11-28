# Wassup Guard: Android-Based File Scanner for WhatsApp Media

**Wassup Guard** is an Android application designed to enhance your mobile security by scanning files received through WhatsApp for potential threats. It provides a real-time defense mechanism, ensuring that your device remains safe from malware and other malicious files.

## Project Overview

The primary goal of this project is to provide a seamless and effective way to scan WhatsApp media files. The app is built with a modern Android tech stack, leveraging the latest tools and libraries to deliver a robust and user-friendly experience. The app's core functionality is centered around a background service that monitors your WhatsApp media folder and automatically scans new files.

## How to Run the Project

To get the project up and running on your local machine, follow these steps:

1.  **Clone the Repository**
    ```sh
    git clone https://github.com/your-username/Wassup-Guard.git
    ```

2.  **Set Up the API Key**
    The project uses the VirusTotal API for threat detection. You will need to get a free API key from the [VirusTotal website](https://www.virustotal.com/gui/join-us).

    *   Create a file named `local.properties` in the root directory of the project.
    *   Add your VirusTotal API key to this file, like so:
        ```properties
        VIRUSTOTAL_API_KEY="YOUR_API_KEY_HERE"
        ```

3.  **Open in Android Studio**
    *   Open Android Studio and select **"Open an Existing Project"**.
    *   Navigate to the cloned repository folder and open it.

4.  **Gradle Sync**
    *   Android Studio should automatically start a Gradle sync. This will download all the necessary dependencies defined in the `build.gradle.kts` files.
    *   If it doesn't start automatically, you can trigger it by clicking the **"Sync Project with Gradle Files"** button in the toolbar.

5.  **Build and Run**
    *   Once the Gradle sync is complete, you can build and run the app.
    *   Select a target device (an emulator or a physical Android device).
    *   Click the **"Run 'app'"** button (the green play icon) in the toolbar.

## Features

*   **Real-time Scanning**: The app runs in the background, automatically scanning new files as they are downloaded to your WhatsApp media folder.
*   **Manual Scanning**: In addition to automatic scanning, you can trigger a manual scan of your WhatsApp media folder at any time.
*   **Threat Detection**: The app uses the VirusTotal API to scan files, providing a reliable and accurate threat assessment.
*   **Quarantine**: Malicious files are automatically quarantined to prevent them from causing harm to your device.
*   **Scan History**: The app maintains a detailed history of all scanned files, allowing you to review past scans and their results.
*   **Modern UI**: The app features a clean and modern user interface, built with Jetpack Compose, that provides a clear overview of the app's status and scan results.

## Tech Stack

*   **Kotlin**: The app is written entirely in Kotlin, a modern and expressive programming language for Android development.
*   **Jetpack Compose**: The user interface is built with Jetpack Compose, a declarative UI toolkit for building native Android apps.
*   **Room**: The app uses the Room persistence library to store scan logs and threat signatures in a local database.
*   **WorkManager**: The background scanning is managed by WorkManager, a powerful and flexible library for deferrable background tasks.
*   **Retrofit**: The app uses Retrofit to make network requests to the VirusTotal API.
*   **Coroutines**: The app uses Kotlin Coroutines to manage asynchronous tasks, such as network requests and database operations.

## Technical Implementation

The app is divided into several key components that work together to provide a comprehensive security solution.

### 1. File Monitoring

The app uses a `FileObserver` to monitor the WhatsApp media folder for new files. When a new file is detected, a `WorkManager` task is triggered to start the scanning process.

### 2. Scanning

The scanning process is handled by a `CoroutineWorker`, which performs the following steps:

1.  **Hashing**: The worker first calculates the SHA-256 hash of the file.
2.  **Local Database Check**: The hash is then checked against a local database of known threat signatures. If a match is found, the file is immediately flagged as malicious.
3.  **VirusTotal API**: If the file is not found in the local database, the hash is sent to the VirusTotal API for a more comprehensive scan. The API returns a detailed report, which includes a list of antivirus engines and their verdicts.
4.  **Verdict**: The app analyzes the VirusTotal report to determine the file's verdict. If the file is identified as malicious, it is flagged as a threat.

### 3. Quarantine

If a file is flagged as malicious, it is moved to a secure quarantine folder. This prevents the file from being opened or executed, effectively neutralizing the threat.

### 4. User Interface

The app's user interface is built with Jetpack Compose and provides the following screens:

*   **Home**: The home screen provides an overview of the app's status, including the number of files scanned, threats detected, and a list of recent scans.
*   **History**: The history screen displays a detailed log of all scanned files and their verdicts.
*   **Quarantine**: The quarantine screen lists all malicious files that have been quarantined. From here, you can choose to delete the files permanently.

## File Hierarchy

The project is structured into logical packages, separating concerns like UI, data, networking, and background work.

```
/app/src/main/java/com/example/wassupguard/
├── data/                 # Handles all data persistence
│   ├── dao/              # Data Access Objects for Room
│   │   ├── ScanLogDao.kt
│   │   └── SignatureDao.kt
│   ├── entity/           # Room entity classes
│   │   ├── ScanLog.kt
│   │   └── Signature.kt
│   └── AppDatabase.kt      # Room database definition
├── network/              # Handles networking with VirusTotal
│   ├── ApiClient.kt
│   ├── VirusTotalApi.kt
│   └── VirusTotalResponse.kt
├── ui/                   # UI layer (Screens and ViewModels)
│   ├── home/
│   │   ├── HomeViewModel.kt
│   │   └── HomeViewModelFactory.kt
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── QuarantineScreen.kt
│   │   ├── QuarantineViewModel.kt
│   │   └── QuarantineViewModelFactory.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/                 # Utility and helper classes
│   ├── HashUtils.kt
│   ├── Notifications.kt
│   ├── QuarantineManager.kt
│   ├── RateLimiter.kt
│   └── SafeScoreCalculator.kt
└── workers/              # Background scanning logic
    └── FileMonitorWorker.kt
```

## Key File Explanations

### Core Application Files

*   `workers/FileMonitorWorker.kt`: The heart of the scanning logic. This `CoroutineWorker` is responsible for hashing files, querying the VirusTotal API, and saving the results to the database.
*   `data/AppDatabase.kt`: Defines the Room database, its tables (entities), and provides access to the DAOs.
*   `network/ApiClient.kt`: Configures and creates the Retrofit client for making requests to the VirusTotal API, including the all-important API key interceptor.
*   `util/QuarantineManager.kt`: A helper object that contains the logic for moving malicious files to a secure quarantine folder and restoring them if the user chooses.

### UI & State Management

*   `ui/screens/HomeScreen.kt`: The main screen of the app, built with Jetpack Compose. It displays the scan dashboard and initiates scans.
*   `ui/home/HomeViewModel.kt`: Manages the state for the `HomeScreen`. It fetches scan logs from the database and exposes them to the UI as a `StateFlow`.
*   `ui/screens/QuarantineScreen.kt`: The screen that displays the list of quarantined files, with buttons to delete or restore them.
*   `ui/screens/QuarantineViewModel.kt`: Manages the state for the `QuarantineScreen`. It filters the scan logs to show only malicious files and handles the delete and restore actions.

### Data Models & DAOs

*   `data/entity/ScanLog.kt`: A data class that defines the schema for the `scan_logs` table in the database. Each instance represents a single scan result.
*   `data/dao/ScanLogDao.kt`: An interface that defines the database operations (e.g., `insert`, `delete`, `observeAll`) for the `scan_logs` table.

## Build & IDE Configuration

These files manage the build process and your local IDE settings.

*   **`.idea/`**: This directory stores all the project-specific settings for Android Studio. It remembers which files you have open, your run configurations, and the layout of your windows. It is excluded from version control.
*   **`build/`**: This is the output folder for the build process. It contains compiled code, processed resources, and the final APK file. It is generated automatically and excluded from version control.
*   **`.gradle/`**: This is a cache directory created by Gradle to make your builds faster. It can be safely deleted and is excluded from version control.
*   **`local.properties`**: This file is for properties specific to your local machine, such as the `VIRUSTOTAL_API_KEY`. It is never shared and is excluded from version control.
*   **`build.gradle.kts` (app-level)**: This is the main build script for the app. It defines the app's dependencies, SDK versions, and includes the logic to load the VirusTotal API key from `local.properties` into the `BuildConfig`.
*   **`.gitignore`**: This file tells Git which files and directories to ignore, such as `.idea`, `build`, and `local.properties`, which is critical for collaboration and security.

## Conclusion

Wassup Guard is a powerful and effective tool for enhancing your mobile security. By leveraging a modern tech stack and a robust set of features, the app provides a comprehensive solution for scanning WhatsApp media files and protecting your device from potential threats.
