# Slate Desktop Companion

A local desktop companion that makes it easy to install and provision [Slate](../android/) on your Android phone over USB.

Open the app in your browser, connect your phone, and click **Install Slate on phone** — the companion handles APK install, Device Owner provisioning, and launching Slate for you.

## Quick start

### 1. Prerequisites

- **Node.js 20+** and npm
- **ADB** (Android platform-tools) on your PATH, or set `ANDROID_HOME`
- **Slate APK** — build once from the repo:

```bash
cd android
./gradlew assembleDebug
```

### 2. Run the companion

```bash
cd companion
npm install
npm run dev
```

Open **http://localhost:5173** in your browser.

For a production-style single-server setup:

```bash
npm run build
npm start
# Open http://localhost:7848
```

## Setup wizard

The companion walks you through:

1. **Prerequisites** — factory reset, USB debugging, no Google account
2. **Connect phone** — auto-detects devices via `adb devices`
3. **Install** — one-click install + `set-device-owner` + launch Slate
4. **Finish** — set Slate as your default home app

### Phone preparation

For Device Owner provisioning to work:

1. Factory reset the phone
2. Complete Android setup **without** adding a Google account
3. Enable **Developer options → USB debugging**
4. Connect via USB and tap **Allow** on the debugging prompt

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SLATE_BRIDGE_PORT` | `7848` | Port for the bridge API and static web UI |
| `SLATE_APK_PATH` | (auto-detect) | Path to a Slate APK file |
| `ANDROID_HOME` | — | Android SDK root (for finding `adb`) |

## Architecture

```
companion/
├── bridge/     # Node.js server — ADB commands + REST API
└── web/        # Vite + React setup wizard UI
```

The bridge exposes a REST API at `/api/*` and serves the built web UI in production mode. During development, Vite proxies API requests to the bridge.

## API (bridge)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/status` | ADB and APK availability |
| `GET` | `/api/devices` | List connected devices |
| `GET` | `/api/devices/:serial` | Device info and pre-flight checks |
| `POST` | `/api/devices/:serial/setup` | Full install + provision + launch |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| ADB not found | Install [platform-tools](https://developer.android.com/tools/releases/platform-tools) or set `ANDROID_HOME` |
| No devices shown | Check USB cable, enable USB debugging, accept the RSA prompt on phone |
| `set-device-owner` fails | Factory reset with no accounts; remove work profile |
| APK not found | Run `cd android && ./gradlew assembleDebug` |
| Unauthorized device | Revoke USB debugging authorizations on phone, reconnect |

## What's next

Policy management (allowlist editing, mode switching, pairing) will connect to the phone's companion sync server once that is implemented on Android. This release focuses on the USB provisioning flow (DC-01 in the functional spec).
