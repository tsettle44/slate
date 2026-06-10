export type StepStatus = "pending" | "running" | "success" | "error" | "warning";

export interface AdbStatus {
  found: boolean;
  path: string | null;
  version: string | null;
  error: string | null;
}

export interface ApkInfo {
  found: boolean;
  path: string | null;
  sizeBytes: number | null;
  sha256: string | null;
  buildHint: string;
}

export interface DeviceInfo {
  serial: string;
  state: string;
  model: string | null;
  manufacturer: string | null;
  androidVersion: string | null;
  sdkVersion: string | null;
  slateInstalled: boolean;
  slateVersion: string | null;
  isDeviceOwner: boolean;
  hasAccounts: boolean;
  hasWorkProfile: boolean;
  usbDebugging: boolean;
  readyForProvisioning: boolean;
  warnings: string[];
  blockers: string[];
}

export interface SetupStepResult {
  id: string;
  label: string;
  status: StepStatus;
  message: string;
  detail?: string;
}

export interface SetupResult {
  success: boolean;
  steps: SetupStepResult[];
}

export interface BridgeStatus {
  adb: AdbStatus;
  apk: ApkInfo;
  serverVersion: string;
}
