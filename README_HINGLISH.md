# Screen Timeout Controller — itel A90

Ye Android app do modes deti hai:

1. **Whole phone:** Android ka `SCREEN_OFF_TIMEOUT` system setting update karti hai. Iske liye Android ka **Modify system settings** special access allow karna hoga.
2. **Only this app:** App screen par visible hone tak `FLAG_KEEP_SCREEN_ON` lagati hai. App se bahar jaate hi ye flag clear ho jata hai.

## APK banane ka tarika (AndroidIDE ya Android Studio)

1. ZIP extract karo.
2. AndroidIDE (Android phone par) ya Android Studio mein `ScreenTimeoutController` folder ko **Open Project** karo.
3. Gradle sync hone do. Internet pehli baar dependencies download karne ke liye chahiye.
4. Build menu se **Build APK(s)** / **Assemble Debug** chalao.
5. APK yahan milega: `app/build/outputs/apk/debug/app-debug.apk`
6. APK install karo. App kholkar **Choose system timeout** dabao. Android settings page khule to **Allow modifying system settings** ON karo, phir app mein wapas aakar timeout dobara select karo.

## Notes

- Project package: `com.kadir.screentimeout`
- Minimum Android: 6.0 (API 23); target SDK 35.
- 1, 2, 3, 5 hours aur maximum Android integer value ka option included hai.
- `Never` option Android ke maximum supported integer value ko set karta hai. Manufacturer firmware (itelOS) ise clamp ya reject kar sakta hai; app is restriction ko bypass nahi karti.
- App-only mode app visible hone par hi screen awake rakhta hai; ye doosri apps mein screen ko awake nahi rakhega.
- Restore button system timeout ko 30 minutes par set karta hai (device ke screenshot mein yahi selected tha).
