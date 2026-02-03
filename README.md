![Logo](resources/icons/icon_colored_round.svg)
#  EBuecherregal
A multiplatform E-Book reader application built with Kotlin and Compose Multiplatform.

## Project Overview

This project is an E-Book reader application that supports multiple platforms, including Android, Desktop (JVM), and Web (Wasm). 
It allows users to read e-books, with easy dictionary lookup.

## Technology Stack

*   **Kotlin:** The primary programming language for the application.
*   **Compose Multiplatform:** The UI framework for building the user interface across all platforms.
*   **SQLite:** Quick storage for metadata.
*   **SQLDelight:** Typesafe SQL queries.
*   **Gradle:** Build tool used for managing dependencies and building the application.

## Building and Running the Application

### On Windows

Use `gradlew.bat` instead of `gradlew`.

### Android

To build and run the development version of the Android app:

```shell
./gradlew :androidApp:assembleDebug
```

### Desktop (JVM)

To build and run the development version of the desktop app:

```shell
./gradlew :desktopApp:run
```

### Web (Wasm)

To build and run the development version of the web app:

```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## Package binaries

Binaries are also built via gradle. 

### Android 
The App can be packaged to `Android App Bundles` and `Android Package Kit`.
```shell
./gradlew :androidApp:bundleRelease  # aab
./gradlew :androidApp:packageRelease # apk
```

### Desktop (JVM)
There are multiple desktop targets configured in the desktop [build.gradle](desktopApp/build.gradle.kts).
To build any given `$TARGET`, e.g. `AppImage` or `Deb`.

**Note:** Desktop targets have to be built on the operating system they target.
**Note:** On Linux, make sure to have the required dependencies to package `deb`, `rpm` or `AppImage`.
**Note:** The `AppImage` target is an app image in the `jpackage` sense, not a single executable, but a directory structure containing all necessary files.

```shell
./gradlew :desktopApp:packageRelease$TARGET
```
Issues may arise due to proguard stripping symbols. Try non-release packaging via:
```shell
./gradlew :desktopApp:package$TARGET
```

## Installation

Download/Build the fitting binary and install it via the default method.
 
### Android 
You can use `adb` of the `Android Debug Bridge`:
```shell
adb -s $DEVICE_NAME install -r $PATH_TO_APK
```

### Windows
Run the given `.msi` installer.

### Linux
Install the fitting package via your distributions `package manager`.
For Arch Linux, check out the `EBuecherregal-bin` package git repository.

