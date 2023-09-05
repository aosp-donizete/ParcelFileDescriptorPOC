# Start service

```bash
adb shell am start-foreground-service com.android.sharefiles/.MainService
```

# Push fake image
```bash
adb push docs/dog.jpeg /storage/emulated/0/Android/data/com.android.sharefiles/cache/real_images_path/
```