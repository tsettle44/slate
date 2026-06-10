import { createHash } from "node:crypto";
import { existsSync } from "node:fs";
import { readFile } from "node:fs/promises";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { execFile } from "node:child_process";
import { promisify } from "node:util";
import type { AdbStatus, ApkInfo, DeviceInfo, SetupResult, SetupStepResult } from "./types.js";

const execFileAsync = promisify(execFile);

export const SLATE_PACKAGE = "com.slate.phone";
export const DEVICE_ADMIN_COMPONENT = `${SLATE_PACKAGE}/.admin.SlateDeviceAdminReceiver`;
export const LAUNCHER_ACTIVITY = `${SLATE_PACKAGE}/.launcher.LauncherActivity`;

const __dirname = dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = resolve(__dirname, "../../..");

function candidateAdbPaths(): string[] {
  const paths: string[] = [];
  const home = process.env.HOME ?? process.env.USERPROFILE ?? "";

  if (process.env.ANDROID_HOME) {
    paths.push(join(process.env.ANDROID_HOME, "platform-tools", "adb"));
    paths.push(join(process.env.ANDROID_HOME, "platform-tools", "adb.exe"));
  }
  if (process.env.ANDROID_SDK_ROOT) {
    paths.push(join(process.env.ANDROID_SDK_ROOT, "platform-tools", "adb"));
    paths.push(join(process.env.ANDROID_SDK_ROOT, "platform-tools", "adb.exe"));
  }
  if (home) {
    paths.push(join(home, "Android", "Sdk", "platform-tools", "adb"));
    paths.push(join(home, "Library", "Android", "sdk", "platform-tools", "adb"));
    paths.push(join(home, "AppData", "Local", "Android", "Sdk", "platform-tools", "adb.exe"));
  }

  paths.push("adb");
  return paths;
}

let cachedAdbPath: string | null | undefined;

export function resolveAdbPath(): string | null {
  if (cachedAdbPath !== undefined) return cachedAdbPath;

  for (const candidate of candidateAdbPaths()) {
    if (candidate === "adb") {
      cachedAdbPath = "adb";
      return cachedAdbPath;
    }
    if (existsSync(candidate)) {
      cachedAdbPath = candidate;
      return cachedAdbPath;
    }
  }

  cachedAdbPath = null;
  return null;
}

async function runAdb(args: string[], serial?: string): Promise<string> {
  const adb = resolveAdbPath();
  if (!adb) throw new Error("ADB not found. Install Android platform-tools and add adb to your PATH.");

  const fullArgs = serial ? ["-s", serial, ...args] : args;
  try {
    const { stdout } = await execFileAsync(adb, fullArgs, { timeout: 120_000 });
    return stdout.trim();
  } catch (error: unknown) {
    const err = error as { stderr?: string; message?: string };
    const detail = err.stderr?.trim() || err.message || "Unknown ADB error";
    throw new Error(detail);
  }
}

export async function getAdbStatus(): Promise<AdbStatus> {
  const path = resolveAdbPath();
  if (!path) {
    return {
      found: false,
      path: null,
      version: null,
      error: "ADB not found. Install Android platform-tools or set ANDROID_HOME.",
    };
  }

  try {
    const version = await runAdb(["version"]);
    const firstLine = version.split("\n")[0] ?? version;
    return { found: true, path, version: firstLine, error: null };
  } catch (error: unknown) {
    return {
      found: false,
      path,
      version: null,
      error: error instanceof Error ? error.message : "Failed to run adb",
    };
  }
}

function candidateApkPaths(): string[] {
  const paths: string[] = [];
  if (process.env.SLATE_APK_PATH) paths.push(resolve(process.env.SLATE_APK_PATH));

  paths.push(join(REPO_ROOT, "android", "app", "build", "outputs", "apk", "debug", "app-debug.apk"));
  paths.push(join(REPO_ROOT, "android", "app", "build", "outputs", "apk", "release", "app-release.apk"));
  paths.push(join(REPO_ROOT, "companion", "assets", "slate.apk"));

  return paths;
}

export async function getApkInfo(): Promise<ApkInfo> {
  for (const path of candidateApkPaths()) {
    if (!existsSync(path)) continue;

    const buffer = await readFile(path);
    const sha256 = createHash("sha256").update(buffer).digest("hex");

    return {
      found: true,
      path,
      sizeBytes: buffer.length,
      sha256,
      buildHint: "Run `cd android && ./gradlew assembleDebug` to build the APK.",
    };
  }

  return {
    found: false,
    path: null,
    sizeBytes: null,
    sha256: null,
    buildHint: "Build the Android app first: `cd android && ./gradlew assembleDebug`",
  };
}

export interface ListedDevice {
  serial: string;
  state: string;
}

export async function listDevices(): Promise<ListedDevice[]> {
  const output = await runAdb(["devices", "-l"]);
  const lines = output.split("\n").slice(1);
  const devices: ListedDevice[] = [];

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    const [serial, state] = trimmed.split(/\s+/);
    if (!serial || !state || state === "offline") continue;
    devices.push({ serial, state });
  }

  return devices;
}

async function shell(serial: string, command: string): Promise<string> {
  return runAdb(["shell", command], serial);
}

async function getProp(serial: string, prop: string): Promise<string | null> {
  try {
    const value = await shell(serial, `getprop ${prop}`);
    return value || null;
  } catch {
    return null;
  }
}

async function isPackageInstalled(serial: string, packageName: string): Promise<boolean> {
  try {
    const output = await shell(serial, `pm path ${packageName}`);
    return output.includes("package:");
  } catch {
    return false;
  }
}

async function getPackageVersion(serial: string, packageName: string): Promise<string | null> {
  try {
    const output = await shell(serial, `dumpsys package ${packageName}`);
    const match = output.match(/versionName=([^\s]+)/);
    return match?.[1] ?? null;
  } catch {
    return null;
  }
}

async function isSlateDeviceOwner(serial: string): Promise<boolean> {
  try {
    const output = await shell(serial, "dpm list-owners");
    return output.includes(SLATE_PACKAGE);
  } catch {
    return false;
  }
}

async function hasDeviceAccounts(serial: string): Promise<boolean> {
  try {
    const output = await shell(serial, "dumpsys account");
    const names = [...output.matchAll(/type=([^\s,]+)/g)].map((m) => m[1]);
    const meaningful = names.filter(
      (t) => !t.includes("com.google.android.gms") && t !== "com.google.work",
    );
    return meaningful.length > 0;
  } catch {
    return false;
  }
}

async function deviceHasWorkProfile(serial: string): Promise<boolean> {
  try {
    const output = await shell(serial, "pm list users");
    return /UserInfo\{10:/.test(output) || output.split("\n").length > 2;
  } catch {
    return false;
  }
}

export async function getDeviceInfo(serial: string): Promise<DeviceInfo> {
  const warnings: string[] = [];
  const blockers: string[] = [];

  const model = await getProp(serial, "ro.product.model");
  const manufacturer = await getProp(serial, "ro.product.manufacturer");
  const androidVersion = await getProp(serial, "ro.build.version.release");
  const sdkVersion = await getProp(serial, "ro.build.version.sdk");

  const slateInstalled = await isPackageInstalled(serial, SLATE_PACKAGE);
  const slateVersion = slateInstalled ? await getPackageVersion(serial, SLATE_PACKAGE) : null;
  const isDeviceOwner = await isSlateDeviceOwner(serial);
  const hasAccounts = await hasDeviceAccounts(serial);
  const hasWorkProfile = await deviceHasWorkProfile(serial);

  if (hasAccounts) {
    blockers.push(
      "Google or other accounts are present. Factory reset the phone (no accounts) before setting Device Owner.",
    );
  }
  if (hasWorkProfile) {
    blockers.push("A work profile is detected. Device Owner cannot be set with an active work profile.");
  }
  if (isDeviceOwner && !slateInstalled) {
    warnings.push("Slate is registered as device owner but the app is not installed.");
  }
  if (!isDeviceOwner && slateInstalled) {
    warnings.push("Slate is installed but not yet set as Device Owner.");
  }
  if (isDeviceOwner) {
    warnings.push("Slate is already the Device Owner on this phone.");
  }

  const sdk = sdkVersion ? parseInt(sdkVersion, 10) : 0;
  if (sdk > 0 && sdk < 31) {
    blockers.push(`Android ${androidVersion ?? sdk} is too old. Slate requires Android 12 (API 31) or newer.`);
  }

  const readyForProvisioning = blockers.length === 0 && !isDeviceOwner;

  return {
    serial,
    state: "device",
    model,
    manufacturer,
    androidVersion,
    sdkVersion,
    slateInstalled,
    slateVersion,
    isDeviceOwner,
    hasAccounts,
    hasWorkProfile,
    usbDebugging: true,
    readyForProvisioning,
    warnings,
    blockers,
  };
}

function step(id: string, label: string, status: SetupStepResult["status"], message: string, detail?: string): SetupStepResult {
  return { id, label, status, message, detail };
}

export async function installApk(serial: string, apkPath: string): Promise<SetupStepResult> {
  try {
    const output = await runAdb(["install", "-r", apkPath], serial);
    const success = output.includes("Success") || output.toLowerCase().includes("success");
    if (!success) {
      return step("install", "Install Slate", "error", "Install failed", output);
    }
    return step("install", "Install Slate", "success", "Slate installed successfully");
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "Install failed";
    let detail = message;
    if (message.includes("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
      detail = "Not enough storage on the phone. Free up space and try again.";
    } else if (message.includes("INSTALL_FAILED_UPDATE_INCOMPATIBLE")) {
      detail = "Existing Slate install is incompatible. Uninstall Slate from the phone and try again.";
    }
    return step("install", "Install Slate", "error", "Could not install Slate", detail);
  }
}

export async function setDeviceOwner(serial: string): Promise<SetupStepResult> {
  try {
    const output = await shell(serial, `dpm set-device-owner ${DEVICE_ADMIN_COMPONENT}`);
    const lower = output.toLowerCase();
    if (lower.includes("success") || lower.includes("device owner set")) {
      return step("provision", "Set Device Owner", "success", "Slate is now the Device Owner");
    }
    if (lower.includes("already")) {
      return step("provision", "Set Device Owner", "success", "Slate is already the Device Owner");
    }
    return step("provision", "Set Device Owner", "error", "Could not set Device Owner", output);
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : "Provisioning failed";
    let detail = message;
    if (message.includes("already has an owner") || message.includes("already several users")) {
      detail =
        "Another app is already Device Owner, or the phone was set up with accounts. Factory reset with no accounts, then try again.";
    } else if (message.includes("not installed")) {
      detail = "Slate must be installed before setting Device Owner.";
    }
    return step("provision", "Set Device Owner", "error", "Provisioning failed", detail);
  }
}

export async function launchLauncher(serial: string): Promise<SetupStepResult> {
  try {
    await runAdb(["shell", "am", "start", "-n", LAUNCHER_ACTIVITY], serial);
    return step("launch", "Open Slate", "success", "Slate launcher opened on your phone");
  } catch (error: unknown) {
    return step(
      "launch",
      "Open Slate",
      "warning",
      "Installed, but could not auto-open Slate",
      error instanceof Error ? error.message : undefined,
    );
  }
}

export async function runFullSetup(serial: string): Promise<SetupResult> {
  const steps: SetupStepResult[] = [];

  const apkInfo = await getApkInfo();
  if (!apkInfo.found || !apkInfo.path) {
    steps.push(step("apk", "Locate APK", "error", "Slate APK not found", apkInfo.buildHint));
    return { success: false, steps };
  }
  steps.push(step("apk", "Locate APK", "success", "Found Slate APK"));

  const device = await getDeviceInfo(serial);
  if (device.blockers.length > 0) {
    for (const blocker of device.blockers) {
      steps.push(step("preflight", "Pre-flight check", "error", blocker));
    }
    return { success: false, steps };
  }
  steps.push(step("preflight", "Pre-flight check", "success", "Phone is ready for setup"));

  if (!device.slateInstalled) {
    const installResult = await installApk(serial, apkInfo.path);
    steps.push(installResult);
    if (installResult.status === "error") return { success: false, steps };
  } else {
    steps.push(step("install", "Install Slate", "success", "Slate is already installed"));
  }

  if (!device.isDeviceOwner) {
    const provisionResult = await setDeviceOwner(serial);
    steps.push(provisionResult);
    if (provisionResult.status === "error") return { success: false, steps };
  } else {
    steps.push(step("provision", "Set Device Owner", "success", "Already provisioned as Device Owner"));
  }

  const launchResult = await launchLauncher(serial);
  steps.push(launchResult);

  const finalDevice = await getDeviceInfo(serial);
  if (finalDevice.isDeviceOwner) {
    steps.push(
      step(
        "complete",
        "Setup complete",
        "success",
        "Slate is installed and provisioned. Set Slate as your default home app on the phone.",
      ),
    );
    return { success: true, steps };
  }

  steps.push(step("complete", "Setup incomplete", "error", "Device Owner was not confirmed after setup"));
  return { success: false, steps };
}

export async function startAdbServer(): Promise<void> {
  try {
    await runAdb(["start-server"]);
  } catch {
    // Best effort — server may already be running
  }
}
