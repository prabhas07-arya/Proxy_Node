# ProxyNode - Build Instructions

## 🚨 IMPORTANT: Manual Setup Required

This app requires **manual installation of RunAnywhere SDK files** before it can build successfully.

## Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** or higher
- **Android SDK** with API 24+ (Android 7.0)
- **Device/Emulator** with 2GB+ RAM

## Step-by-Step Setup

### 1. Clone Repository

```bash
git clone <your-repo-url>
cd ProxyNode
```

### 2. **CRITICAL: Download RunAnywhere SDK AARs**

The app will NOT build without these files. You need to:

1. **Create libs directory:**
   ```bash
   mkdir -p app/libs
   ```

2. **Download these AAR files:**
    - `RunAnywhereKotlinSDK-release.aar` (4.0MB)
    - `runanywhere-llm-llamacpp-release.aar` (2.1MB)

3. **Download from:
   ** [RunAnywhere SDK Releases](https://github.com/RunanywhereAI/runanywhere-sdks/releases/tag/android-v0.1.2-alpha)

4. **Place files in:** `app/libs/`
   ```
   app/libs/
   ├── RunAnywhereKotlinSDK-release.aar
   └── runanywhere-llm-llamacpp-release.aar
   ```

### 3. Sync Project

```bash
# In Android Studio: File → Sync Project with Gradle Files
# Or via command line:
./gradlew build
```

### 4. Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Or click "Run" in Android Studio
```

## Common Errors & Solutions

### ❌ "Could not find RunAnywhereKotlinSDK-release.aar"

**Solution:** Download and place AAR files in `app/libs/` as described above.

### ❌ "Unresolved reference: RunAnywhere"

**Solution:** AAR files are missing. Download them from RunAnywhere releases.

### ❌ "Unresolved reference: Icons.Default.Mic"

**Solution:** The `material-icons-extended` dependency should fix this. If not, sync project.

### ❌ "Unresolved reference: ExperimentalPermissionsApi"

**Solution:** The `accompanist-permissions` dependency should fix this. If not, sync project.

### ❌ Build fails with "Cannot resolve symbol"

**Solution:**

1. Clean project: `./gradlew clean`
2. Rebuild: `./gradlew build`
3. Invalidate caches in Android Studio: File → Invalidate Caches and Restart

## File Structure After Setup

```
ProxyNode/
├── app/
│   ├── libs/                                    # ← AAR files go here
│   │   ├── RunAnywhereKotlinSDK-release.aar    # ← Download required
│   │   └── runanywhere-llm-llamacpp-release.aar # ← Download required
│   ├── src/main/java/com/proxynode/feedbackapp/
│   │   ├── ProxyNodeApplication.kt
│   │   ├── presentation/
│   │   └── data/
│   └── build.gradle.kts
├── build.gradle.kts
└── README.md
```

## First Run Setup

1. **Launch app**
2. **Complete onboarding** (3 screens)
3. **Wait for AI model download** (~119MB SmolLM2 model)
4. **Grant permissions** when prompted:
    - Internet (for model download)
    - Microphone (for voice input)
5. **Start using** the feedback system!

## Testing Without SDK

If you want to test the UI without RunAnywhere SDK:

1. **Comment out RunAnywhere imports** in these files:
    - `ProxyNodeApplication.kt`
    - `FeedbackAnalyzer.kt`
    - `FeedbackRepository.kt`

2. **Use fallback mode:** The app has rule-based fallbacks for all AI features.

## Performance Notes

- **First launch:** Slow due to model download (119MB)
- **Subsequent launches:** Fast (model cached locally)
- **RAM usage:** 2GB+ recommended for AI processing
- **Storage:** ~200MB total with models

## Troubleshooting

### If build still fails:

1. **Check Android Studio version** (needs Hedgehog+)
2. **Update Gradle** to latest version
3. **Clean project:** `./gradlew clean`
4. **Invalidate caches:** File → Invalidate Caches and Restart
5. **Check JDK version** (needs JDK 17+)
6. **Verify AAR files** are in correct location with correct names

### If app crashes:

1. **Check device RAM** (needs 2GB+)
2. **Check storage space** (needs 200MB+)
3. **Grant all permissions** when prompted
4. **Check logcat** for detailed error messages

## Alternative: Demo Mode

To run without RunAnywhere SDK (UI only):

1. Replace all AI calls with mock data
2. Comment out SDK initialization
3. Use fallback text processing methods

This will let you see the UI and flow without the AI functionality.

---

**Need Help?** Check the main README.md for detailed documentation and troubleshooting.