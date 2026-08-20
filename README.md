#  BrainBox

**BrainBox** is a gamified Android learning application designed to help Grade 12 learners practise **Mathematics** and **Physical Sciences** in a more interactive way.

The project combines quizzes, study resources, progress tracking, leaderboards, user accounts, and Firebase services in one mobile learning experience.

## Overview

BrainBox was developed as an Android application focused on educational technology and practical software development.

The application provides learners with subject-based content and interactive quizzes while using gamification features to encourage regular practice and progress.

##  Features

-  **Interactive quizzes** for Mathematics and Physical Sciences
-  **Topic-based learning** and study hubs
-  **Past-paper and PDF resources**
-  **Gamification** through points, trophies, progress tracking, and leaderboards
-  **User accounts** with registration, login, password recovery, profile editing, and password changes
-  **Progress tracking** and quiz history
-  **Game modes** for a more engaging learning experience
-  **Firebase Authentication** for account management
-  **Firebase Firestore** for application data
-  **Firebase Storage** for stored resources
-  **Jetpack Compose UI** with Material 3
-  **Lottie animations** and custom app sounds
-  **Admin features** for quiz and user management

##  Technologies

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary programming language |
| **Jetpack Compose** | Modern Android UI development |
| **Material 3** | UI components and design system |
| **Firebase Authentication** | User authentication and account management |
| **Cloud Firestore** | Application data storage |
| **Firebase Storage** | File and resource storage |
| **Android Navigation Compose** | Screen navigation |
| **Lottie Compose** | Animations |
| **Kotlin Coroutines** | Asynchronous operations |
| **Gradle Kotlin DSL** | Project and dependency configuration |

##  Project Structure

```text
BrainBox/
├── app/
│   └── src/
│       ├── androidTest/
│       └── main/
│           ├── java/com/example/brainbox/
│           │   ├── data/
│           │   ├── navigation/
│           │   └── ui/
│           │       ├── components/
│           │       ├── screens/
│           │       └── theme/
│           └── res/
│               ├── drawable/
│               ├── mipmap-*/
│               ├── raw/
│               ├── values/
│               └── xml/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

##  Getting Started

### Prerequisites

- Android Studio
- JDK 11 or a compatible JDK supported by your Android Studio/Gradle setup
- A configured Firebase project for the BrainBox Android application

## Running the Application

- Clone the repository
- Open the project in Android Studio.
- Allow Android Studio to sync the Gradle project and download the required dependencies.
- Connect an Android device or start an Android emulator.
- Build and run the application from Android Studio.


##  Testing

The project includes unit and Android instrumentation test scaffolding. Tests can be run from Android Studio or with Gradle using the appropriate test task for the configured environment.

##  Project Status

**Version:** `1.0`

This project is a portfolio and learning project focused on Android application development, Firebase integration, UI design, and educational technology.

## Skills Demonstrated

- Android application development
- Kotlin programming
- Jetpack Compose UI development
- Material 3 design
- Firebase integration
- Authentication and user management
- Cloud Firestore data management
- Application navigation
- CRUD functionality
- Quiz and scoring logic
- Progress tracking
- UI/UX implementation
- Asynchronous programming with Kotlin Coroutines
- Working with Android resources and animations
- Git and GitHub version control
- Structuring and maintaining an Android project

##  Author

**Phumla Donovely Mashaba**

ICT graduate / Software developer.

GitHub: [@pmashaba-dev](https://github.com/pmashaba-dev)

##  License

No open-source license has currently been added to this repository. Until a license is added, the source code should not be assumed to be available for redistribution or commercial use.
