#!/usr/bin/env bash
set -euo pipefail

BOX_DIR="${1:-/tmp/Box}"
OUT_DIR="${2:-/tmp/box-apk}"

if [[ ! -d "$BOX_DIR" ]]; then
  echo "Box repo not found at: $BOX_DIR"
  exit 1
fi

BOX_SHA=$(git -C "$BOX_DIR" rev-parse --short HEAD 2>/dev/null || echo "unknown")
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR/app/src/main/java/com/example/boxwrapper" "$OUT_DIR/app/src/main/res/values"

cat > "$OUT_DIR/settings.gradle.kts" <<'EOF'
pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "BoxWrapper"
include(":app")
EOF

cat > "$OUT_DIR/build.gradle.kts" <<'EOF'
plugins {
  id("com.android.application") version "8.7.3" apply false
  id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
EOF

cat > "$OUT_DIR/gradle.properties" <<'EOF'
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
EOF

cat > "$OUT_DIR/app/build.gradle.kts" <<'EOF'
plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.example.boxwrapper"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.example.boxwrapper"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
}
EOF

cat > "$OUT_DIR/app/src/main/AndroidManifest.xml" <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <application
      android:allowBackup="true"
      android:label="Box Wrapper"
      android:supportsRtl="true">
    <activity
        android:name=".MainActivity"
        android:exported="true">
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
    </activity>
  </application>
</manifest>
EOF

cat > "$OUT_DIR/app/src/main/java/com/example/boxwrapper/MainActivity.kt" <<EOF
package com.example.boxwrapper

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val tv = TextView(this)
    tv.text = "Box source commit: $BOX_SHA"
    tv.textSize = 20f
    setContentView(tv)
  }
}
EOF

cat > "$OUT_DIR/app/src/main/res/values/strings.xml" <<'EOF'
<resources>
  <string name="app_name">Box Wrapper</string>
</resources>
EOF

if ! command -v gradle >/dev/null 2>&1; then
  echo "gradle command is required (GitHub Action sets this up via gradle/actions/setup-gradle)."
  exit 1
fi

cd "$OUT_DIR"
gradle wrapper --gradle-version 8.10.2
./gradlew --no-daemon assembleDebug

find "$OUT_DIR/app/build/outputs/apk" -name "*.apk" -print
