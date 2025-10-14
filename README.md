# MediaPipe LLM Android Quickstart

## Chuẩn bị
- Android Studio (JDK 17)
- Thiết bị Android (Pixel 8 / S23 trở lên được khuyến nghị)
- Model `.task` (ví dụ: Gemma-3 1B 4-bit)

## Chạy cục bộ (Android Studio)
1. Mở project.
2. Cắm điện thoại (bật USB debugging) → Run app.
3. Lần đầu, nhấn **"Chọn model (.task) lần đầu"** và chọn file `.task` (đã chép sẵn vào thư mục Download).
4. Nhập prompt → **Generate** (kết quả sẽ stream ra màn hình).

## Build APK bằng GitHub Actions
- Mỗi lần push lên `main`, workflow sẽ build **app-debug.apk** và đính kèm làm artifact.
- Tải APK: Tab **Actions** → chọn workflow run → **Artifacts** → `app-debug-apk`.

## Cài APK lên máy
- Cách 1: Dùng **adb**:
  ```bash
  adb install -r app-debug.apk
