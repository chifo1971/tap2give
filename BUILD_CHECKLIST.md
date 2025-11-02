# Tap2Give Build Checklist

## Current Version Info
- **Version**: v1.2.1-beta
- **Build Number**: 11

---

## Pre-Build Checklist

### For Every Build
- [ ] Increment build number in `app/build.gradle` (`versionCode`)
- [ ] Update version name if needed (`versionName`)
- [ ] Test on real device (phone and/or tablet)
- [ ] Verify environment variables are set:
  - `KEYSTORE_PASSWORD`
  - `KEY_PASSWORD`

### For Significant Changes Only
Run Detekt code quality check for:
- **New features** (e.g., adding receipt system, new payment methods)
- **Major refactoring** (e.g., restructuring payment flow)
- **Production releases** (e.g., v1.3.0, v1.4.0, v2.0.0)

**Skip Detekt for:**
- Daily development builds
- Bug fixes
- Minor UI tweaks
- Testing builds

**Command:**
```bash
./gradlew detekt
```

**Report Location:**
`app/build/reports/detekt/detekt.html`

### Before Production Release
- [ ] Run Detekt code quality check: `./gradlew detekt`
- [ ] Review Detekt report and fix critical issues
- [ ] Check Firestore rules match code expectations
- [ ] Verify all Firebase Functions are deployed:
  - `createConnectionToken`
  - `sendDonationReceipt`
  - `sendSmsReceipt`
- [ ] Test receipt delivery (email and SMS)
- [ ] Test on both phone and tablet devices

---

## Build Commands

### Debug Build
```bash
KEYSTORE_PASSWORD=YourPassword KEY_PASSWORD=YourPassword ./gradlew assembleDebug
```

### Release Build
```bash
KEYSTORE_PASSWORD=YourPassword KEY_PASSWORD=YourPassword ./gradlew clean assembleRelease
```

### Install on Device
```bash
# Debug
adb -s <DEVICE_ID> install -r app/build/outputs/apk/debug/app-debug.apk

# Release
adb -s <DEVICE_ID> install -r app/build/outputs/apk/release/app-release.apk
```

---

## Version Naming Convention

- **Major releases**: v1.0.0, v2.0.0 (breaking changes)
- **Minor releases**: v1.1.0, v1.2.0 (new features)
- **Patch releases**: v1.2.1, v1.2.2 (bug fixes)
- **Beta releases**: v1.3.0-beta, v1.3.1-beta

---

## Firestore Rules Checklist

Ensure these collections have appropriate rules:
- `/mosques/{mosqueCode}` - Public read for receipt viewer
- `/receipts/{receiptId}` - Public read for web receipts
- `/payments/{paymentId}` - Secure write from app, read from Functions

---

## Post-Build Verification

- [ ] App installs without errors
- [ ] Stripe Terminal initializes correctly
- [ ] Payment flow works end-to-end
- [ ] Receipt delivery works (email/SMS)
- [ ] Kiosk mode activates on designated devices
- [ ] Device detection works (phone vs tablet)
- [ ] App survives force-stop and auto-restarts (kiosk devices)

---

## Notes

- Always test on real hardware before releasing
- Keep build numbers sequential (never reuse)
- Tag releases in git: `git tag v1.3.0`
- Document breaking changes in release notes
