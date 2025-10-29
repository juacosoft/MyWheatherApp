# Project Constitution

## 1. Project Identity

### 1.1 Purpose
Android weather application providing real-time climate data through a modern, maintainable, and testable codebase that follows industry best practices and clean architecture principles.

### 1.2 Core Values
- **Code Quality**: Maintainable, testable, and well-documented code
- **User Experience**: Intuitive, responsive, and accessible interfaces
- **Technical Excellence**: Adherence to architectural patterns and modern Android practices
- **Test Coverage**: Comprehensive unit testing for business logic
- **Semantic Versioning**: Clear, meaningful commit and release conventions

## 2. Technical Architecture

### 2.1 Platform Requirements
- **Target Platform**: Android
- **Minimum API Level**: 24 (Android 7.0)
- **Target Language**: Kotlin
- **UI Framework**: Jetpack Compose

### 2.2 Architectural Principles

#### Clean Architecture
The application MUST follow Clean Architecture with clear separation of concerns:

```
presentation/ (UI, ScreenModels)
    └─> domain/ (Use Cases, Business Logic, Domain Models)
        └─> data/ (Repositories, Data Sources, DTOs)
```

**Layer Responsibilities**:
- **Presentation**: UI components (Composables), state management (ScreenModels), user interactions
- **Domain**: Business logic, use cases, domain models, repository interfaces
- **Data**: Repository implementations, API clients, local storage, data mappers

**Layer Rules**:
- Domain layer MUST NOT depend on presentation or data layers
- Domain layer MUST contain only pure Kotlin (no Android dependencies)
- Data layer MUST implement interfaces defined in domain
- Dependencies flow inward: presentation → domain ← data

#### MVI Pattern (Model-View-Intent)
All presentation layer MUST follow MVI pattern:

- **Model**: Represents the UI state
- **View**: Composable functions that render the state
- **Intent**: User actions/events that trigger state changes

**Implementation Requirements**:
- Use `ScreenModel` from Voyager (NOT Android ViewModel)
- Extend `MVIBaseScreenModel` for all screen models
- Implement `MVIContract` for each screen (State, Event, Effect)
- State MUST be immutable (use data classes with val)
- Effects MUST be one-time events (navigation, snackbars, dialogs)

### 2.3 Core Libraries

#### Mandatory Dependencies
- **Dependency Injection**: Koin
- **Navigation**: Voyager
- **HTTP Client**: Ktor Client
- **UI**: Jetpack Compose

#### Planned Dependencies
- **Local Storage**: Room (for entity caching) or DataStore (for preferences)
- **Theming**: Material 3 with light/dark theme support

### 2.4 External Dependencies
- **Weather API**: OpenWeatherMap.org API
- API keys MUST be stored securely (NOT in version control)
- API responses MUST be validated and error-handled

## 3. Code Quality Standards

### 3.1 Testing Requirements

#### Mandatory Test Coverage
- **Data Layer**: ALL repository implementations, data sources, mappers
- **Domain Layer**: ALL use cases and business logic
- **Test Coverage Target**: Minimum 80% for data and domain layers

#### Testing Libraries
- **Mocking**: MockK
- **Test Framework**: JUnit 4
- **Flow Testing**: Turbine
- **Coroutines Testing**: kotlinx-coroutines-test

#### Testing Principles
- Use Given-When-Then structure for test organization
- Test happy paths AND error cases
- Mock external dependencies (API, database)
- Use meaningful test names: `functionName_scenario_expectedResult`
- Each test MUST be independent and repeatable

Example:
```kotlin
@Test
fun `getWeatherData_whenApiReturnsSuccess_emitsWeatherData`() {
    // Given
    val expected = mockWeatherData()
    coEvery { api.fetchWeather() } returns expected

    // When
    val result = repository.getWeatherData()

    // Then
    result.test {
        assertEquals(expected, awaitItem())
        awaitComplete()
    }
}
```

### 3.2 Code Style

#### Kotlin Conventions
- Follow official Kotlin coding conventions
- Use meaningful variable/function names (no single letters except loops)
- Prefer immutability (val over var)
- Use type inference when obvious
- Maximum function length: 30 lines (split complex logic)
- Maximum file length: 300 lines (split large files)

#### Compose Guidelines
- Composables MUST be stateless when possible
- Hoist state to appropriate level
- Use remember { } for computed values
- Prefer @Stable and @Immutable annotations
- Preview annotations for all screens/components

#### Dependency Injection
- Use constructor injection
- Define Koin modules by feature
- Avoid service locator pattern (no direct Koin.get() in production code)

### 3.3 Error Handling
- Use sealed classes for API responses (Success, Error, Loading)
- Never swallow exceptions silently
- Log errors appropriately
- Provide user-friendly error messages
- Handle network unavailability gracefully

### 3.4 Documentation
- Public APIs MUST have KDoc comments
- Complex logic MUST have explanatory comments
- README.md MUST be kept up to date
- Architecture decisions MUST be documented

## 4. Development Workflow

### 4.1 Git Commit Convention

#### Semantic Commits
ALL commits MUST follow semantic commit format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring (no functional change)
- `test`: Adding or updating tests
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `perf`: Performance improvements
- `chore`: Build, dependencies, tooling

**Examples**:
```
feat(weather): add hourly forecast screen

Implements new screen showing 24-hour forecast with temperature graph
Uses Voyager navigation and MVI pattern

Closes #123
```

```
test(repository): add unit tests for weather data mapper

Covers success and error scenarios for API response mapping
Achieves 95% coverage for WeatherMapper class
```

### 4.2 Branch Strategy
- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: New features
- `fix/*`: Bug fixes
- `refactor/*`: Code improvements

### 4.3 Code Review Requirements
- At least one approval required
- All tests MUST pass
- No unresolved comments
- Code coverage requirements met

## 5. Architecture Decision Records

### 5.1 Why ScreenModel over ViewModel?
**Decision**: Use Voyager's `ScreenModel` instead of Android's `ViewModel`

**Rationale**:
- Better integration with Voyager navigation
- Lifecycle tied to screen rather than Android Activity/Fragment
- Simplifies testing (no Android framework dependencies)
- Consistent with multi-platform potential

### 5.2 Why Koin over Dagger/Hilt?
**Decision**: Use Koin for dependency injection

**Rationale**:
- Simpler setup and configuration
- Pure Kotlin (no annotation processing)
- Better error messages
- Sufficient for current project scale
- Easier testing with module overrides

### 5.3 Why Ktor over Retrofit?
**Decision**: Use Ktor Client for HTTP communication

**Rationale**:
- Kotlin-first design
- Multiplatform ready
- Coroutine native
- Lightweight and flexible

## 6. Quality Gates

### 6.1 Definition of Done
A feature is considered complete when:
- [ ] Code implements acceptance criteria
- [ ] Unit tests written for data and domain layers (80%+ coverage)
- [ ] Code reviewed and approved
- [ ] No lint warnings or errors
- [ ] Documentation updated
- [ ] Semantic commit created
- [ ] Builds successfully
- [ ] Manual testing completed

### 6.2 Pre-commit Checklist
Before committing code:
- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] No commented-out code (remove or justify)
- [ ] No TODO comments (create issues instead)
- [ ] No hardcoded values (use constants or configuration)
- [ ] No secrets or API keys in code

### 6.3 Pre-release Checklist
Before releasing:
- [ ] All features tested on minimum API level (24)
- [ ] All features tested on latest API level
- [ ] Different screen sizes tested
- [ ] Error scenarios tested (no network, API errors)
- [ ] Release notes prepared
- [ ] Version bumped appropriately

## 7. Future Roadmap Considerations

### 7.1 Planned Features
- **Local Caching**: Implement Room database for offline support
- **User Preferences**: DataStore for settings persistence
- **Theming**: Material 3 dark/light theme support
- **Widgets**: Home screen widgets for quick weather view
- **Notifications**: Weather alerts and daily forecasts

### 7.2 Technical Debt Prevention
- Regular dependency updates
- Continuous refactoring of complex code
- Performance monitoring and optimization
- Accessibility improvements
- Security audits

### 7.3 Scalability Considerations
- Modularization by feature (when project grows)
- Multi-module architecture preparation
- Potential multiplatform expansion (iOS, Desktop)
- CI/CD pipeline implementation

## 8. Non-Negotiables

These principles MUST NEVER be compromised:

1. **No business logic in presentation layer** - Always in domain/use cases
2. **All business logic MUST be tested** - Data and domain layers require tests
3. **Immutable state** - No mutable state in ScreenModels
4. **Dependency rule** - Dependencies always point inward (presentation → domain ← data)
5. **Single responsibility** - Each class/function has one reason to change
6. **Semantic commits** - All commits follow semantic convention
7. **No secrets in code** - API keys, tokens in secure configuration
8. **MVIContract implementation** - All screens use base contract and ScreenModel

## 9. Enforcement

### 9.1 Automated Checks
- Lint checks in CI/CD pipeline
- Unit test execution on every commit
- Code coverage reports
- Static code analysis

### 9.2 Manual Reviews
- Architecture review for significant changes
- Code review for all pull requests
- Quarterly technical debt assessment

## 10. Amendment Process

This constitution is a living document. Amendments require:
1. Discussion and consensus among team members
2. Documentation of rationale for change
3. Update of affected documentation and templates
4. Communication to all stakeholders

**Last Updated**: 2025-10-29
**Version**: 1.0.0