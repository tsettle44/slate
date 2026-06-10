import { useCallback, useEffect, useState } from "react";
import {
  formatBytes,
  getDevice,
  getDevices,
  getStatus,
  runSetup,
  type BridgeStatus,
  type DeviceInfo,
  type ListedDevice,
  type SetupResult,
} from "../api/client";
import Card from "../components/Card";
import StatusBadge from "../components/StatusBadge";
import "./Setup.css";

type WizardStep = "welcome" | "connect" | "install" | "complete";

const PREREQUISITES = [
  {
    title: "Factory reset your phone",
    detail: "Device Owner can only be set on a fresh device with no Google or other accounts.",
  },
  {
    title: "Complete minimal Android setup",
    detail: "Skip adding a Google account for now. You can add one after Slate is provisioned.",
  },
  {
    title: "Enable USB debugging",
    detail: "Settings → About phone → tap Build number 7 times → Developer options → USB debugging.",
  },
  {
    title: "Connect via USB",
    detail: "Use a data cable and tap Allow on the phone when prompted for USB debugging.",
  },
];

export default function Setup() {
  const [wizardStep, setWizardStep] = useState<WizardStep>("welcome");
  const [bridgeStatus, setBridgeStatus] = useState<BridgeStatus | null>(null);
  const [devices, setDevices] = useState<ListedDevice[]>([]);
  const [selectedSerial, setSelectedSerial] = useState<string | null>(null);
  const [deviceInfo, setDeviceInfo] = useState<DeviceInfo | null>(null);
  const [setupResult, setSetupResult] = useState<SetupResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [polling, setPolling] = useState(false);

  const refreshStatus = useCallback(async () => {
    try {
      const status = await getStatus();
      setBridgeStatus(status);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Cannot reach companion bridge");
    }
  }, []);

  const refreshDevices = useCallback(async () => {
    try {
      const { devices: found } = await getDevices();
      setDevices(found);
      if (found.length === 1 && !selectedSerial) {
        setSelectedSerial(found[0].serial);
      }
      setError(null);
    } catch (e) {
      setDevices([]);
      setError(e instanceof Error ? e.message : "Failed to detect devices");
    }
  }, [selectedSerial]);

  const refreshDeviceInfo = useCallback(async (serial: string) => {
    try {
      const info = await getDevice(serial);
      setDeviceInfo(info);
      setError(null);
    } catch (e) {
      setDeviceInfo(null);
      setError(e instanceof Error ? e.message : "Failed to read device info");
    }
  }, []);

  useEffect(() => {
    void refreshStatus();
  }, [refreshStatus]);

  useEffect(() => {
    if (wizardStep !== "connect") return;
    setPolling(true);
    void refreshDevices();
    const interval = setInterval(() => void refreshDevices(), 2500);
    return () => {
      clearInterval(interval);
      setPolling(false);
    };
  }, [wizardStep, refreshDevices]);

  useEffect(() => {
    if (!selectedSerial || wizardStep === "welcome") return;
    void refreshDeviceInfo(selectedSerial);
  }, [selectedSerial, wizardStep, refreshDeviceInfo]);

  const handleInstall = async () => {
    if (!selectedSerial) return;
    setLoading(true);
    setSetupResult(null);
    setError(null);
    try {
      const result = await runSetup(selectedSerial);
      setSetupResult(result);
      await refreshDeviceInfo(selectedSerial);
      setWizardStep(result.success ? "complete" : "install");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Setup failed");
    } finally {
      setLoading(false);
    }
  };

  const canInstall =
    bridgeStatus?.adb.found &&
    bridgeStatus?.apk.found &&
    selectedSerial &&
    deviceInfo?.readyForProvisioning;

  const deviceLabel = deviceInfo
    ? [deviceInfo.manufacturer, deviceInfo.model].filter(Boolean).join(" ") || selectedSerial
    : selectedSerial;

  return (
    <div className="setup">
      <div className="wizard-progress">
        {(["welcome", "connect", "install", "complete"] as WizardStep[]).map((step, index) => (
          <div
            key={step}
            className={`wizard-dot ${wizardStep === step ? "active" : ""} ${
              ["welcome", "connect", "install", "complete"].indexOf(wizardStep) > index ? "done" : ""
            }`}
          />
        ))}
      </div>

      {error && (
        <div className="alert alert-error" role="alert">
          {error}
        </div>
      )}

      {wizardStep === "welcome" && (
        <Card
          title="Install Slate on your phone"
          subtitle="This wizard walks you through installing Slate and setting it up as your phone's device owner — all from your computer."
        >
          <ol className="prereq-list">
            {PREREQUISITES.map((item) => (
              <li key={item.title}>
                <strong>{item.title}</strong>
                <span>{item.detail}</span>
              </li>
            ))}
          </ol>

          <div className="system-checks">
            <h3>System check</h3>
            <ul>
              <li className={bridgeStatus?.adb.found ? "ok" : "bad"}>
                {bridgeStatus?.adb.found ? "✓" : "✗"} ADB{" "}
                {bridgeStatus?.adb.found
                  ? `(${bridgeStatus.adb.version ?? "ready"})`
                  : "— install Android platform-tools"}
              </li>
              <li className={bridgeStatus?.apk.found ? "ok" : "bad"}>
                {bridgeStatus?.apk.found ? "✓" : "✗"} Slate APK{" "}
                {bridgeStatus?.apk.found
                  ? `(${formatBytes(bridgeStatus.apk.sizeBytes ?? 0)})`
                  : `— ${bridgeStatus?.apk.buildHint ?? "build required"}`}
              </li>
            </ul>
          </div>

          <button
            className="btn btn-primary"
            onClick={() => setWizardStep("connect")}
            disabled={!bridgeStatus?.adb.found}
          >
            {bridgeStatus?.apk.found ? "Continue" : "Continue anyway"}
          </button>
        </Card>
      )}

      {wizardStep === "connect" && (
        <Card
          title="Connect your phone"
          subtitle="Plug in your phone with USB debugging enabled. We'll detect it automatically."
        >
          <div className="connect-status">
            <span className={`pulse ${polling ? "on" : ""}`} />
            {devices.length === 0
              ? "Waiting for a device…"
              : `${devices.length} device${devices.length === 1 ? "" : "s"} found`}
          </div>

          {devices.length > 0 && (
            <div className="device-list">
              {devices.map((d) => (
                <button
                  key={d.serial}
                  type="button"
                  className={`device-item ${selectedSerial === d.serial ? "selected" : ""}`}
                  onClick={() => setSelectedSerial(d.serial)}
                >
                  <span className="device-icon">📱</span>
                  <span>
                    <strong>{d.serial}</strong>
                    <small>{d.state}</small>
                  </span>
                </button>
              ))}
            </div>
          )}

          {deviceInfo && (
            <div className="device-details">
              <h3>Phone status</h3>
              <dl>
                <div>
                  <dt>Android</dt>
                  <dd>{deviceInfo.androidVersion ?? "—"}</dd>
                </div>
                <div>
                  <dt>Slate installed</dt>
                  <dd>{deviceInfo.slateInstalled ? `Yes (${deviceInfo.slateVersion ?? "unknown"})` : "No"}</dd>
                </div>
                <div>
                  <dt>Device Owner</dt>
                  <dd>{deviceInfo.isDeviceOwner ? "Yes" : "Not yet"}</dd>
                </div>
              </dl>

              {deviceInfo.blockers.length > 0 && (
                <div className="alert alert-error">
                  <strong>Action required</strong>
                  <ul>
                    {deviceInfo.blockers.map((b) => (
                      <li key={b}>{b}</li>
                    ))}
                  </ul>
                </div>
              )}

              {deviceInfo.warnings.length > 0 && (
                <div className="alert alert-warning">
                  <ul>
                    {deviceInfo.warnings.map((w) => (
                      <li key={w}>{w}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}

          <div className="btn-row">
            <button className="btn btn-ghost" onClick={() => setWizardStep("welcome")}>
              Back
            </button>
            <button
              className="btn btn-primary"
              disabled={!selectedSerial}
              onClick={() => setWizardStep("install")}
            >
              Continue
            </button>
          </div>
        </Card>
      )}

      {wizardStep === "install" && (
        <Card
          title="Install Slate"
          subtitle={
            deviceLabel
              ? `Ready to install on ${deviceLabel}. This takes about a minute.`
              : "Install and provision Slate on your connected phone."
          }
        >
          {!bridgeStatus?.apk.found && (
            <div className="alert alert-warning">
              <strong>APK not found.</strong> Build it first:{" "}
              <code>cd android && ./gradlew assembleDebug</code>
            </div>
          )}

          {deviceInfo && !deviceInfo.readyForProvisioning && deviceInfo.blockers.length > 0 && (
            <div className="alert alert-error">
              Fix the issues on the previous step before installing.
            </div>
          )}

          <div className="install-preview">
            <div className="install-step-preview">
              <span>1</span> Install Slate APK
            </div>
            <div className="install-step-preview">
              <span>2</span> Set Slate as Device Owner
            </div>
            <div className="install-step-preview">
              <span>3</span> Open Slate on your phone
            </div>
          </div>

          <button
            className="btn btn-primary btn-large"
            onClick={() => void handleInstall()}
            disabled={!canInstall || loading}
          >
            {loading ? "Installing…" : "Install Slate on phone"}
          </button>

          {setupResult && (
            <div className="setup-results">
              <h3>Setup log</h3>
              <ul>
                {setupResult.steps.map((step) => (
                  <li key={`${step.id}-${step.label}`} className={`result-${step.status}`}>
                    <StatusBadge status={step.status} />
                    <div>
                      <strong>{step.label}</strong>
                      <p>{step.message}</p>
                      {step.detail && <small>{step.detail}</small>}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="btn-row">
            <button className="btn btn-ghost" onClick={() => setWizardStep("connect")}>
              Back
            </button>
          </div>
        </Card>
      )}

      {wizardStep === "complete" && (
        <Card title="You're all set!" subtitle="Slate is installed and provisioned on your phone.">
          <div className="success-hero">✓</div>

          <ol className="next-steps">
            <li>
              <strong>Set Slate as your default home app</strong>
              <span>
                On your phone, press Home and choose Slate → Always. Or go to Settings → Apps → Default
                apps → Home app.
              </span>
            </li>
            <li>
              <strong>Open Slate</strong>
              <span>You should see the minimalist launcher home screen with a clock and app list.</span>
            </li>
            <li>
              <strong>Configure your allowlist</strong>
              <span>
                Policy management from the companion is coming soon. For now, use the launcher on your
                phone.
              </span>
            </li>
          </ol>

          {deviceInfo?.isDeviceOwner && (
            <div className="alert alert-success">
              Device Owner confirmed — Slate can enforce your app policy.
            </div>
          )}

          <button className="btn btn-primary" onClick={() => setWizardStep("connect")}>
            Connect another phone
          </button>
        </Card>
      )}
    </div>
  );
}
