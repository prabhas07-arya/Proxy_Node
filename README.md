# ProxyNode - Anonymous Student Feedback App

A secure Android application built with Kotlin and Jetpack Compose that provides anonymous student
feedback using on-device AI processing via the RunAnywhere SDK.

## What This App Does

ProxyNode solves the problem of unsafe, ignored, or biased student feedback by providing a
completely anonymous and private feedback system that processes all data locally on the user's
device.

## Features

- **Anonymous Feedback Submission**: Students can submit text or voice feedback completely
  anonymously
- **On-Device AI Processing**: Uses RunAnywhere SDK to analyze and categorize feedback locally
- **Automatic Anonymization**: AI removes personal identifiers while preserving message meaning
- **Smart Categorization**: Automatically tags feedback as "Academics", "Infrastructure", or "
  Placement"
- **Voice Input Support**: Record feedback using voice (converted to text locally)
- **Secure Local Storage**: All data stored in encrypted local database
- **Privacy-First Design**: No data ever leaves the device
- **Modern Material 3 UI**: Clean, professional interface with dark/light mode support

## Architecture

### Technology Stack

- **Kotlin** - Primary language
- **Jetpack Compose** - Modern UI toolkit
- **RunAnywhere SDK** - On-device AI processing
- **Room Database** - Local data storage with encryption
- **MVVM Pattern** - Clean architecture with ViewModels and Repositories
- **Material 3** - Latest Material Design components

### Core Components

```
├── presentation/
│   ├── MainActivity.kt          # Main entry point
│   ├── navigation/              # Navigation setup
│   ├── screen/
│   │   ├── onboarding/          # Welcome screens
│   │   ├── feedback/            # Feedback submission
│   │   ├── history/             # User's feedback history
│   │   └── admin/               # Demo admin dashboard
│   └── theme/                   # Material 3 theming
├── data/
│   ├── model/                   # Data classes
│   ├── database/                # Room database setup
│   ├── ai/                      # RunAnywhere SDK integration
│   └── repository/              # Data access layer
└── ProxyNodeApplication.kt      # App initialization
```

## Quick Start

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17 or higher
- Android device/emulator with API 24+ (Android 7.0)
- RunAnywhere SDK AAR files (see Installation)

### Installation

1. **Clone and Setup**
   ```bash
   git clone <your-repo>
   cd ProxyNode
   ```

2. **Add RunAnywhere SDK AARs**

   Place these files in `app/libs/`:
    - `RunAnywhereKotlinSDK-release.aar`
    - `runanywhere-llm-llamacpp-release.aar`

   Download
   from: [RunAnywhere SDK Releases](https://github.com/RunanywhereAI/runanywhere-sdks/releases)

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   # Or open in Android Studio and click Run
   ```

### First Launch

1. **Onboarding**: Learn about privacy features
2. **AI Initialization**: Wait for on-device model to download (SmolLM2 360M ~119MB)
3. **Submit Feedback**: Write or record your thoughts
4. **View Results**: Check History and Admin tabs

## How It Works

### Privacy-First Process

1. **Input**: Student writes or speaks feedback
2. **Local AI Processing**: RunAnywhere SDK processes on-device
    - Removes personal identifiers (names, IDs, etc.)
    - Summarizes into one clear sentence
    - Categorizes as Academics/Infrastructure/Placement
3. **Secure Storage**: Encrypted local database saves processed feedback
4. **Display**: Anonymized feedback appears in admin dashboard

### Key Privacy Features

- **100% On-Device**: All AI processing happens locally
- **No Network Calls**: After model download, works completely offline
- **Automatic Anonymization**: AI removes personal details
- **Encrypted Storage**: Local database is encrypted
- **No Tracking**: No analytics or user identification

## Technical Details

### RunAnywhere SDK Integration

```kotlin
// AI Processing
class FeedbackAnalyzer {
    suspend fun analyzeFeedback(text: String): FeedbackResult {
        // 1. Anonymize personal information
        val anonymized = anonymizeText(text)

        // 2. Generate summary
        val summary = summarizeText(anonymized)

        // 3. Classify category
        val tag = classifyText(anonymized)

        return FeedbackResult(text, anonymized, summary, tag)
    }
}
```

### Database Schema

```kotlin
@Entity(tableName = "feedback")
data class FeedbackEntity(
    val id: Long,
    val originalText: String,      // User input (encrypted)
    val anonymizedText: String,    // AI-processed anonymous version
    val summary: String,           // One-line summary
    val tag: String,              // Category (Academics/Infrastructure/Placement)
    val isVoiceInput: Boolean,    // Input method
    val timestamp: Date,          // When submitted
    val deviceId: String          // Anonymous device identifier
)
```

### Permissions Required

```xml

<uses-permission android:name="android.permission.INTERNET" />           <!-- Model download -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />       <!-- Voice input -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
android:maxSdkVersion="28" />                            <!-- Model storage -->
```

## App Screens

### 1. Onboarding

- Privacy explanation
- AI processing overview
- Offline capabilities

### 2. Feedback Submission

- Text input with rich editing
- Voice recording with real-time conversion
- AI status indicator
- Privacy reminders

### 3. History

- User's previous feedback
- Summaries and categories
- Submission timestamps
- Delete options

### 4. Admin Dashboard (Demo)

- Anonymized feedback overview
- Category statistics
- Filter by tag
- Test data generation

## Development

### Running Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Code Structure

- **MVVM Architecture**: ViewModels handle business logic
- **Repository Pattern**: Centralized data access
- **Dependency Injection**: Manual DI for simplicity
- **Coroutines**: Async operations and UI updates
- **StateFlow**: Reactive UI state management

### Adding New Features

1. **New Screen**: Add to `presentation/screen/`
2. **Data Model**: Update `data/model/`
3. **Database**: Modify `data/database/`
4. **Repository**: Extend `data/repository/`
5. **Navigation**: Update `navigation/ProxyNodeNavigation.kt`

## Security Considerations

### Data Protection

- All sensitive data encrypted at rest
- Personal identifiers automatically removed
- No cloud storage or external APIs
- Secure key management with Android Keystore

### AI Model Security

- Models verified during download
- Sandboxed execution environment
- No data leaves device during processing
- Fallback to rule-based processing if AI fails

## Performance

### Resource Usage

- **Model Size**: 119MB (SmolLM2 360M)
- **RAM Required**: 2GB+ recommended
- **Storage**: ~200MB with models
- **Battery**: Minimal impact (local processing)

### Optimization Tips

- Model loads on first use (not app startup)
- Background processing for large text
- Efficient UI updates with StateFlow
- Database queries optimized with indexes

## Troubleshooting

### Common Issues

**AI Model Won't Download**

- Check internet connection
- Verify sufficient storage space
- Retry from AI status card

**App Crashes During Generation**

- Ensure device has 2GB+ RAM
- Close other apps to free memory
- Try shorter feedback text

**Voice Recording Not Working**

- Grant microphone permission
- Check device has microphone
- Restart app if permission granted recently

**Database Errors**

- Clear app data to reset database
- Check device storage availability
- Update to latest app version

## Future Enhancements

### Planned Features

- [ ] Multiple language support
- [ ] Advanced sentiment analysis
- [ ] Batch feedback processing
- [ ] Export anonymized data
- [ ] Custom feedback categories
- [ ] Voice recognition improvements
- [ ] Offline speech-to-text

### Technical Improvements

- [ ] Model fine-tuning for education domain
- [ ] Better encryption methods
- [ ] Performance optimizations
- [ ] Automated testing suite
- [ ] CI/CD pipeline

## Contributing

This is a demonstration app built for educational purposes. The core concept shows how on-device AI
can solve real privacy concerns in educational feedback systems.

### Development Setup

1. Fork the repository
2. Create feature branch
3. Add RunAnywhere SDK AARs
4. Test on physical device
5. Submit pull request

## License

This example app follows the license terms of the RunAnywhere SDK. See individual components for
their specific licenses.

## Resources

- [RunAnywhere SDK Documentation](https://github.com/RunanywhereAI/runanywhere-sdks)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material 3 Design System](https://m3.material.io/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)

---
**Built with ❤️ using RunAnywhere SDK for privacy-first on-device AI**

*ProxyNode demonstrates the future of secure, anonymous feedback systems in education*
