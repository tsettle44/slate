#!/usr/bin/env bash
set -euo pipefail

printf '>>> [install:%s] start\n' ""

{
  export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"

  if [ -z "${ANDROID_HOME:-}" ] && [ -d "$HOME/Android/Sdk" ]; then
    export ANDROID_HOME="$HOME/Android/Sdk"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
  fi

  if [ -n "${ANDROID_HOME:-}" ]; then
    export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
  else
    export PATH="$JAVA_HOME/bin:$PATH"
  fi

  if [ -f android/gradlew ]; then
    if [ -n "${ANDROID_HOME:-}" ] && [ ! -f android/local.properties ]; then
      printf 'sdk.dir=%s\n' "$ANDROID_HOME" > android/local.properties
    fi
    (cd android && ./gradlew --version > /dev/null 2>&1) || true
  fi
}

printf '<<< [install:%s] complete\n' ""
