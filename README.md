![Logo](resources/logos/icon_colored_round.svg)
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
./gradlew :composeApp:assembleDebug
```

### Desktop (JVM)

To build and run the development version of the desktop app:

```shell
./gradlew :composeApp:run
```

### Web (Wasm)

To build and run the development version of the web app:

```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
