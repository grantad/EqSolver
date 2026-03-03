# Quick Setup Guide

Follow these steps to get EqSolver running on your device:

## Step 1: Get Claude API Key

1. Go to https://console.anthropic.com/
2. Sign up or log in
3. Create a new API key
4. Copy the key (starts with `sk-ant-...`)

## Step 2: Configure Your API Key

Create a file named `local.properties` in the project root with this content:

```properties
CLAUDE_API_KEY=sk-ant-api-your-actual-key-here
```

Replace `sk-ant-api-your-actual-key-here` with your actual Claude API key.

## Step 3: Open in Android Studio

1. Launch Android Studio
2. Click "Open" and select the EqSolver folder
3. Wait for Gradle sync to complete (this may take a few minutes on first run)

## Step 4: Run the App

### Option A: On a Physical Device
1. Enable Developer Options on your Android phone:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging in Developer Options
3. Connect your phone via USB
4. Click the green "Run" button in Android Studio
5. Select your device from the list

### Option B: On an Emulator
1. In Android Studio, click Tools > Device Manager
2. Create a new virtual device (recommended: Pixel 5, API 33+)
3. Click the green "Run" button
4. Select your emulator from the list

## Step 5: Test the App

1. Grant camera permission when prompted
2. Point camera at a printed math equation
3. Tap the camera button to capture
4. Wait for the AI to solve it (requires internet)

## Sample Equations to Test

Try these equations to test the app:

- Simple: `2x + 5 = 15`
- Quadratic: `x² - 5x + 6 = 0`
- Fraction: `(x + 3) / 2 = 5`
- System: `2x + y = 10, x - y = 2`

## Troubleshooting

**"API key not configured"**
- Make sure `local.properties` file exists in the project root
- Verify the API key is correct
- Rebuild: Build > Clean Project, then Build > Rebuild Project

**Gradle sync fails**
- Check your internet connection
- Try File > Invalidate Caches > Invalidate and Restart

**Camera permission denied**
- Go to phone Settings > Apps > EqSolver > Permissions
- Enable Camera permission

**App crashes on launch**
- Check logcat in Android Studio for error messages
- Verify your device is running Android 8.0 or higher

## Need Help?

If you encounter issues:
1. Check the full README.md for detailed documentation
2. Look at logcat output in Android Studio (View > Tool Windows > Logcat)
3. Verify all dependencies downloaded correctly
