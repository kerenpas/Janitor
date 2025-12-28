# Janitor - Garbage Collection Trip Optimizer

An Android application that calculates the optimal number of trips needed to collect garbage bags based on weight constraints.

## Overview

Janitor helps optimize garbage collection by calculating the minimum number of trips required to carry multiple garbage bags, considering a maximum carrying capacity of 3.0 kg per trip. The app uses an efficient algorithm to pair bags optimally.

## Features

- **Add Garbage Bags**: Input bag weights between 1.01 kg and 3.0 kg
- **Input Validation**: Real-time validation with helpful error messages
- **Optimal Trip Calculation**: Smart pairing algorithm to minimize trips
- **Trip Visualization**: View detailed trip breakdown showing which bags go together
- **Clean UI**: Modern Material 3 design with Jetpack Compose
- **Error Handling**: Comprehensive input validation and error states

## Tech Stack

### Architecture
- **MVVM Pattern**: Clear separation of concerns
- **MVI (Model-View-Intent)**: Unidirectional data flow
- **Clean Architecture**: Domain, Presentation, and UI layers

### Technologies
- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: Modern declarative UI
- **Hilt**: Dependency injection
- **Coroutines & Flow**: Async operations and state management
- **Material 3**: Latest Material Design components

### Testing
- **JUnit 4**: Unit testing framework
- **MockK**: Mocking library
- **Turbine**: Flow testing
- **Google Truth**: Fluent assertions
- **Coroutines Test**: Testing coroutines

## Requirements

- Android Studio Hedgehog or later
- JDK 17 or higher
- Android SDK 24+ (minimum)
- Android SDK 36 (target)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/kerenpas/Janitor.git
cd Janitor
```

### Open in Android Studio

1. Open Android Studio
2. Click "Open an Existing Project"
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete

### Build and Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build and run
./gradlew installDebug
```

Or simply click the "Run" button in Android Studio.

## Testing

### Run Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run tests with coverage
./gradlew testDebugUnitTest

# Run specific test class
./gradlew test --tests "CalculateTripsUseCaseTest"
```

### Run Lint

```bash
./gradlew lintDebug
```

### Test Coverage

The project includes comprehensive test coverage:
- **CalculateTripsUseCase**: 13 test cases covering business logic
- **JanitorViewModel**: 17 test cases covering state management
- **Total**: 30+ unit tests

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/hitechs/janitor/
│   │   │   ├── domain/              # Business logic
│   │   │   │   ├── models.kt        # Data models
│   │   │   │   └── CalculateTripsUseCase.kt
│   │   │   ├── presntation/         # ViewModels and UI state
│   │   │   │   ├── JanitorViewModel.kt
│   │   │   │   ├── UIState.kt
│   │   │   │   └── JanitorScreen.kt
│   │   │   ├── ui/theme/            # Theming
│   │   │   ├── MainActivity.kt
│   │   │   └── JanitorApplication.kt
│   │   └── AndroidManifest.xml
│   └── test/
│       └── java/com/hitechs/janitor/
│           ├── domain/
│           │   └── CalculateTripsUseCaseTest.kt
│           └── presntation/
│               └── JanitorViewModelTest.kt
├── build.gradle.kts
└── proguard-rules.pro
```

## Algorithm

The app uses a **two-pointer greedy algorithm** to optimize bag pairing:

1. **Separate bags**: Heavy bags (>1.99 kg) and light bags (≤1.99 kg)
2. **Heavy bags**: Each gets its own trip (exceeds pairing limit)
3. **Light bags optimization**:
   - Sort light bags in descending order
   - Use two pointers (heaviest and lightest)
   - Try to pair: if sum ≤ 3.0 kg, pair them; otherwise, send heavier bag alone
   - Continue until all bags are assigned

**Time Complexity**: O(n log n) - dominated by sorting
**Space Complexity**: O(n) - for storing trips

## CI/CD

GitHub Actions workflow runs on every push and pull request:

- ✅ Lint checks
- ✅ Unit tests
- ✅ Build debug APK
- ✅ Upload artifacts

View the workflow: `.github/workflows/android-ci.yml`

## Input Constraints

- **Minimum weight**: 1.01 kg
- **Maximum weight**: 3.0 kg
- **Maximum capacity per trip**: 3.0 kg
- **Valid input formats**: Decimal numbers (e.g., 1.5, 2.3)

## Dependencies

See `gradle/libs.versions.toml` for complete dependency list.

### Main Dependencies
- Compose BOM: 2024.09.00
- Hilt: 2.52
- Material 3
- Lifecycle Runtime KTX: 2.10.0

### Test Dependencies
- JUnit: 4.13.2
- MockK: 1.13.13
- Turbine: 1.2.0
- Google Truth: 1.4.4
- Coroutines Test: 1.9.0

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Write unit tests for new features
- Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Dependency injection with [Hilt](https://dagger.dev/hilt/)
- Testing with [MockK](https://mockk.io/) and [Turbine](https://github.com/cashapp/turbine)

---

Made with ❤️ using Kotlin and Jetpack Compose
