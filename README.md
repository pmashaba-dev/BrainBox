# 🧠 Brain Box

**Brain Box** is a gamified Android learning application designed to help Grade 12 learners practise **Mathematics** and **Physical Sciences** in a more interactive way.

The project combines quizzes, study resources, progress tracking, leaderboards, user accounts, and Firebase services in one mobile learning experience.

## ✨ Features

- 🎯 **Interactive quizzes** for Mathematics and Physical Sciences
- 📚 **Topic-based learning** and study hubs
- 📝 **Past-paper and PDF resources**
- 🏆 **Gamification** through points, trophies, progress tracking, and leaderboards
- 👤 **User accounts** with registration, login, password recovery, profile editing, and password changes
- 📊 **Progress tracking** and quiz history
- 🎮 **Game modes** for a more engaging learning experience
- 🔐 **Firebase Authentication** for account management
- ☁️ **Firebase Firestore** for application data
- 📦 **Firebase Storage** for stored resources
- 🎨 **Jetpack Compose UI** with Material 3
- ✨ **Lottie animations** and custom app sounds
- 🛠️ **Admin features** for quiz and user management

## 🛠️ Technologies

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

## 🏗️ Project Structure

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

## 🚀 Getting Started

### Prerequisites

- Android Studio
- JDK 11 or a compatible JDK supported by your Android Studio/Gradle setup
- A configured Firebase project for the Brain Box Android application

### Installation

1. Clone the repository:

```bash
git clone https://github.com/pmashaba-dev/BrainBox.git
```

2. Open the project in Android Studio.

3. Allow Gradle to sync and download the required dependencies.

4. Make sure `app/google-services.json` matches your Firebase Android project.

5. Connect an Android device or start an Android emulator.

6. Run the `app` configuration from Android Studio.

## 🔥 Firebase Configuration

Brain Box uses Firebase services for authentication, application data, and file storage.

For your own Firebase project, create an Android app using the package name:

```text
com.example.brainbox
```

Then download your Firebase configuration file and place it at:

```text
app/google-services.json
```

Before deploying a production version, configure appropriate Firebase Security Rules for Firestore and Storage and review your Firebase/Google Cloud API key restrictions.

## 🧪 Testing

The project includes unit and Android instrumentation test scaffolding. Tests can be run from Android Studio or with Gradle using the appropriate test task for the configured environment.

## 📌 Project Status

**Version:** `1.0`

This project is a portfolio and learning project focused on Android application development, Firebase integration, UI design, and educational technology.

## 👨‍💻 Author

**Phumla Donovely Mashaba**

ICT graduate and Android application developer.

GitHub: [@pmashaba-dev](https://github.com/pmashaba-dev)

## 📄 License

No license has been added to this repository yet. Until a license is added, the repository should be treated as source-available rather than automatically granting permission for reuse or redistribution.
