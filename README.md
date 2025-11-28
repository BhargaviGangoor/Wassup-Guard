# Wassup Guard: Android-Based File Scanner for WhatsApp Media

**Wassup Guard** is an Android application designed to enhance your mobile security by scanning files received through WhatsApp for potential threats. It provides a real-time defense mechanism, ensuring that your device remains safe from malware and other malicious files.

## Project Overview

The primary goal of this project is to provide a seamless and effective way to scan WhatsApp media files. The app is built with a modern Android tech stack, leveraging the latest tools and libraries to deliver a robust and user-friendly experience. The app's core functionality is centered around a background service that monitors your WhatsApp media folder and automatically scans new files.

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

## Conclusion

Wassup Guard is a powerful and effective tool for enhancing your mobile security. By leveraging a modern tech stack and a robust set of features, the app provides a comprehensive solution for scanning WhatsApp media files and protecting your device from potential threats.
