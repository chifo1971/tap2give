# Tap2Give - Mosque Donation Kiosk

A Kotlin Android application designed for mosque donation kiosks with Material 3 design, bilingual support (English/Arabic), and integrated payment processing.

## Features

### Core Functionality
- **2x2 Grid Layout**: Large, touch-friendly donation amount buttons ($5, $10, $25, $50, $100, Custom)
- **Bilingual Support**: English and Arabic text with RTL support
- **Kiosk Mode**: Full-screen, portrait-locked interface with screen always on
- **Payment Processing**: Card and NFC payment options with Stripe Terminal SDK
- **Sound Effects**: Audio feedback for user interactions and payment status

### Technical Specifications
- **Target SDK**: 33
- **Min SDK**: 26
- **Package**: com.mosque.taptogive
- **Design**: Material 3 with custom green color scheme
- **Architecture**: MVVM with Navigation Component

## Project Structure

```
app/
├── src/main/
│   ├── java/com/mosque/taptogive/
│   │   ├── MainActivity.kt              # Main donation selection screen
│   │   ├── PaymentActivity.kt           # Payment processing screen
│   │   └── NfcActivity.kt              # NFC payment handling
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml       # Main screen layout
│   │   │   └── activity_payment.xml    # Payment screen layout
│   │   ├── values/
│   │   │   ├── strings.xml             # English strings
│   │   │   ├── colors.xml              # Color scheme
│   │   │   ├── themes.xml              # Material 3 themes
│   │   │   └── arrays.xml              # String arrays
│   │   ├── values-ar/
│   │   │   └── strings.xml             # Arabic strings
│   │   ├── drawable/                   # Icons and graphics
│   │   ├── anim/                       # Transition animations
│   │   ├── navigation/
│   │   │   └── nav_graph.xml           # Navigation graph
│   │   └── raw/                        # Audio files
│   └── AndroidManifest.xml             # App configuration
├── build.gradle                        # App-level dependencies
└── proguard-rules.pro                  # ProGuard configuration
```

## Setup Instructions

### 1. Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26+
- Kotlin 1.9.10+
- Gradle 8.0+

### 2. Firebase Configuration
1. Create a Firebase project at https://console.firebase.google.com
2. Add your Android app with package name: `com.mosque.taptogive`
3. Download `google-services.json` and place it in the `app/` directory
4. Enable the following Firebase services:
   - Firestore Database
   - Remote Config
   - Crashlytics
   - Analytics

### 3. Stripe Configuration
1. Create a Stripe account at https://dashboard.stripe.com
2. Get your publishable key from the Stripe dashboard
3. Update the publishable key in `PaymentActivity.kt`:
   ```kotlin
   Terminal.initTerminal(
       applicationContext,
       "pk_test_your_publishable_key_here", // Replace with your key
       this
   )
   ```

### 4. Audio Files
Add the following audio files to `app/src/main/res/raw/`:
- `button_click.wav` - Button press sound
- `success.wav` - Payment success sound
- `error.wav` - Payment error sound

See `app/src/main/res/raw/README_AUDIO.md` for detailed audio requirements.

### 5. Build and Run
1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run on a device or emulator

## Dependencies

### Core Android
- `androidx.core:core-ktx:1.12.0`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.10.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`

### Navigation
- `androidx.navigation:navigation-fragment-ktx:2.7.5`
- `androidx.navigation:navigation-ui-ktx:2.7.5`

### Firebase
- `com.google.firebase:firebase-bom:32.7.0`
- `com.google.firebase:firebase-firestore-ktx`
- `com.google.firebase:firebase-config-ktx`
- `com.google.firebase:firebase-crashlytics-ktx`
- `com.google.firebase:firebase-analytics-ktx`

### Payment Processing
- `com.stripe:stripeterminal:3.0.0`

### Audio
- `androidx.media:media:1.7.0`

## Permissions

The app requires the following permissions:
- `android.permission.NFC` - For NFC payment processing
- `android.permission.INTERNET` - For network communication
- `android.permission.WAKE_LOCK` - To keep screen on in kiosk mode
- `android.permission.WRITE_EXTERNAL_STORAGE` - For logging and data storage
- `android.permission.ACCESS_NETWORK_STATE` - For network status checking
- `android.permission.VIBRATE` - For haptic feedback

## Kiosk Mode Configuration

The app is configured for kiosk mode with:
- Full-screen display
- Portrait orientation locked
- Screen always on
- Back button disabled
- Home button handling
- Excluded from recent apps

## Customization

### Color Scheme
The app uses a custom green color scheme suitable for mosque environments:
- Primary: #2E7D32 (Dark Green)
- Secondary: #4CAF50 (Light Green)
- Custom colors defined in `res/values/colors.xml`

### Donation Amounts
Default amounts are $5, $10, $25, $50, $100, and Custom. These can be modified in:
- `res/values/strings.xml` and `res/values-ar/strings.xml`
- `MainActivity.kt` click handlers

### Remote Configuration
The app supports Firebase Remote Config for:
- Dynamic donation amounts
- Color scheme updates
- Feature toggles
- A/B testing

## Security Considerations

- All payment processing is handled through Stripe Terminal SDK
- No sensitive payment data is stored locally
- NFC communication is encrypted
- Firebase security rules should be configured for Firestore

## Testing

### Unit Tests
- Add unit tests for business logic
- Test payment processing flows
- Test audio feedback functionality

### Integration Tests
- Test Firebase integration
- Test Stripe Terminal integration
- Test NFC functionality

### Device Testing
- Test on various screen sizes
- Test in kiosk mode
- Test with different NFC cards
- Test audio output

## Deployment

### Production Build
1. Update Stripe keys to production
2. Configure Firebase for production
3. Enable ProGuard for code obfuscation
4. Sign the APK with release keystore
5. Test thoroughly before deployment

### Kiosk Deployment
1. Install the app on the kiosk device
2. Set the app as the default launcher
3. Configure device settings for kiosk mode
4. Disable unnecessary system features
5. Set up monitoring and maintenance procedures

## Support

For technical support or feature requests, please contact the development team.

## License

This project is proprietary software. All rights reserved.

---

**Note**: This is a template project. Before deploying to production, ensure all security measures are properly implemented and thoroughly tested.
