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

export interface BridgeStatus {
  adb: AdbStatus;
  apk: ApkInfo;
  serverVersion: string;
}

export interface ListedDevice {
  serial: string;
  state: string;
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

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, init);
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.error ?? `Request failed (${response.status})`);
  }
  return response.json() as Promise<T>;
}

export function getStatus(): Promise<BridgeStatus> {
  return fetchJson("/api/status");
}

export function getDevices(): Promise<{ devices: ListedDevice[] }> {
  return fetchJson("/api/devices");
}

export function getDevice(serial: string): Promise<DeviceInfo> {
  return fetchJson(`/api/devices/${encodeURIComponent(serial)}`);
}

export function runSetup(serial: string): Promise<SetupResult> {
  return fetchJson(`/api/devices/${encodeURIComponent(serial)}/setup`, { method: "POST" });
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
