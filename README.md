# EqSolver - AI-Powered Equation Solver for Android

An Android app that uses your phone's camera to capture mathematical equations and provides step-by-step solutions using Claude AI.

## Features

- **Camera Integration**: Capture equations using your phone's camera with CameraX
- **OCR Text Recognition**: Extract equations from images using Google ML Kit (on-device)
- **AI-Powered Solutions**: Get detailed step-by-step solutions using Claude API
- **Clean Material Design UI**: Modern, intuitive interface
- **Share Solutions**: Share equation solutions with others

## Tech Stack

- **Language**: Java
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Camera**: CameraX library
- **OCR**: Google ML Kit Text Recognition
- **AI**: Claude API (Anthropic)
- **HTTP**: OkHttp
- **JSON**: Gson

## Setup Instructions

### 1. Prerequisites

- Android Studio (latest version recommended)
- Android device or emulator running Android 8.0 or higher
- Claude API key from Anthropic

### 2. Get Your Claude API Key

1. Visit [Anthropic Console](https://console.anthropic.com/)
2. Sign up or log in
3. Navigate to API Keys section
4. Create a new API key

### 3. Configure API Key

1. Copy `local.properties.template` to `local.properties`:
   ```bash
   cp local.properties.template local.properties
   ```

2. Open `local.properties` and add your Claude API key:
   ```properties
   CLAUDE_API_KEY=your_api_key_here
   ```

3. **IMPORTANT**: Never commit `local.properties` to version control (it's already in `.gitignore`)

### 4. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files (File > Sync Project with Gradle Files)
3. Connect your Android device or start an emulator
4. Click Run or press Shift+F10

## Project Structure

```
EqSolver/
├── app/
│   ├── src/main/
│   │   ├── java/com/eqsolver/
│   │   │   ├── MainActivity.java          # Welcome screen
│   │   │   ├── CameraActivity.java        # Camera capture & processing
│   │   │   ├── ResultActivity.java        # Display solutions
│   │   │   ├── api/
│   │   │   │   └── ClaudeApiClient.java   # Claude API integration
│   │   │   ├── ml/
│   │   │   │   └── TextRecognizer.java    # ML Kit text recognition
│   │   │   └── models/
│   │   │       └── Solution.java          # Data model
│   │   ├── res/                           # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle                       # App-level dependencies
├── build.gradle                           # Project-level build config
└── settings.gradle                        # Gradle settings
```

## How It Works

1. **Capture**: User taps "Get Started" and takes a photo of a mathematical equation
2. **Extract**: Google ML Kit processes the image on-device to extract text
3. **Solve**: Extracted equation is sent to Claude API for step-by-step solution
4. **Display**: Solution is displayed with the original image and extracted equation
5. **Share**: Users can share solutions via standard Android share menu

## Permissions

The app requires the following permissions:
- **Camera**: To capture images of equations
- **Internet**: To communicate with Claude API

## Building for Release

1. Update version code and name in `app/build.gradle`
2. Generate a signed APK or AAB:
   - Build > Generate Signed Bundle / APK
   - Follow the wizard to create/use a keystore
3. ProGuard rules are already configured for release builds

## Troubleshooting

### "API key not configured" error
- Make sure you created `local.properties` from the template
- Verify your Claude API key is correctly set
- Rebuild the project (Build > Clean Project, then Build > Rebuild Project)

### Camera not working
- Grant camera permission when prompted
- Check that your device/emulator has a working camera
- Try restarting the app

### Text recognition fails
- Ensure the equation is clearly visible and well-lit
- Try capturing from a flat surface with good contrast
- Currently optimized for printed text; handwritten equations may have lower accuracy

### API call fails
- Check your internet connection
- Verify your Claude API key is valid and has credits
- Check Claude API status at status.anthropic.com

## Future Enhancements

- Support for handwritten equations (via Cloud Vision API)
- History of solved equations
- Offline mode with cached solutions
- LaTeX rendering for better equation display
- iOS version

## License

This project is for educational purposes.

## Credits

- Camera: [CameraX](https://developer.android.com/training/camerax)
- Text Recognition: [Google ML Kit](https://developers.google.com/ml-kit)
- AI: [Claude API by Anthropic](https://www.anthropic.com/)
