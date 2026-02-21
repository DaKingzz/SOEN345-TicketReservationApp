# Ticket Reservation App

An Android application for browsing and reserving event tickets, built as part of SOEN 345 at Concordia University.

## Team

| Name | Student ID |
|---|---|
| Sergio Abreo Alvarez | 40274677 |
| Arturo Sanchez | 40283236 |
| Robert Louis Lando | 40275679 |
| Thomas Shizhuo Chen | 40274703 |

## Overview

The Ticket Reservation App allows users to register, log in, and browse upcoming events to reserve tickets. Authentication is handled via Firebase, supporting both email/password and phone number (OTP) registration.

## Features

- Email and password registration and login
- Phone number registration with SMS OTP verification
- User profiles stored in Firestore
- Password reset via email
- Persistent login session

## Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (min SDK 24, target SDK 35) |
| Language | Java |
| Backend | Firebase Authentication + Firestore |
| Build | Gradle 8.6, AGP 8.3.2 |
| UI | Material Components 1.12.0 |
| Testing | JUnit 5 |

## Getting Started

### Prerequisites

- Android Studio (Hedgehog or later)
- Java 17
- A Firebase project with **Email/Password** and **Phone** authentication enabled

### Setup

1. Clone the repository
2. Open the project in Android Studio and let it sync
3. Obtain `google-services.json` from your team's Firebase project (see a teammate — this file is not in the repo)
4. Place `google-services.json` in the `app/` directory
5. Run the app on an emulator or physical device

> `google-services.json` is excluded from version control intentionally. Never commit it.

## CI / GitHub Actions

Three automated jobs run on every push and pull request to `main`:

| Job | Description |
|---|---|
| Unit Tests | Runs local JUnit 5 tests via `gradle testDebugUnitTest` |
| Lint | Runs `gradle lintDebug` to catch code quality issues |
| Build APK | Assembles a debug APK (runs only if the above two pass) |

## Running Tests Locally

```bash
./gradlew testDebugUnitTest
```

Test reports are generated at `app/build/reports/tests/testDebugUnitTest/`.
