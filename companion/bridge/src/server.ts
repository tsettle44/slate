import cors from "cors";
import express from "express";
import { existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import {
  getAdbStatus,
  getApkInfo,
  getDeviceInfo,
  installApk,
  launchLauncher,
  listDevices,
  runFullSetup,
  setDeviceOwner,
  startAdbServer,
} from "./adb.js";

const PORT = Number(process.env.SLATE_BRIDGE_PORT ?? 7848);
const __dirname = dirname(fileURLToPath(import.meta.url));
const WEB_DIST = join(__dirname, "../../web/dist");

const app = express();
app.use(cors());
app.use(express.json());

app.get("/api/status", async (_req, res) => {
  const [adb, apk] = await Promise.all([getAdbStatus(), getApkInfo()]);
  res.json({ adb, apk, serverVersion: "0.1.0" });
});

app.get("/api/devices", async (_req, res) => {
  try {
    const devices = await listDevices();
    res.json({ devices });
  } catch (error: unknown) {
    res.status(500).json({
      error: error instanceof Error ? error.message : "Failed to list devices",
      devices: [],
    });
  }
});

app.get("/api/devices/:serial", async (req, res) => {
  try {
    const info = await getDeviceInfo(req.params.serial);
    res.json(info);
  } catch (error: unknown) {
    res.status(500).json({
      error: error instanceof Error ? error.message : "Failed to read device info",
    });
  }
});

app.post("/api/devices/:serial/install", async (req, res) => {
  try {
    const apk = await getApkInfo();
    if (!apk.found || !apk.path) {
      res.status(400).json({ error: apk.buildHint });
      return;
    }
    const result = await installApk(req.params.serial, apk.path);
    res.json(result);
  } catch (error: unknown) {
    res.status(500).json({
      error: error instanceof Error ? error.message : "Install failed",
    });
  }
});

app.post("/api/devices/:serial/provision", async (req, res) => {
  try {
    const result = await setDeviceOwner(req.params.serial);
    res.json(result);
  } catch (error: unknown) {
    res.status(500).json({
      error: error instanceof Error ? error.message : "Provisioning failed",
    });
  }
});

app.post("/api/devices/:serial/launch", async (req, res) => {
  try {
    const result = await launchLauncher(req.params.serial);
    res.json(result);
  } catch (error: unknown) {
    res.status(500).json({
      error: error instanceof Error ? error.message : "Launch failed",
    });
  }
});

app.post("/api/devices/:serial/setup", async (req, res) => {
  try {
    const result = await runFullSetup(req.params.serial);
    res.json(result);
  } catch (error: unknown) {
    res.status(500).json({
      success: false,
      steps: [],
      error: error instanceof Error ? error.message : "Setup failed",
    });
  }
});

if (existsSync(WEB_DIST)) {
  app.use(express.static(WEB_DIST));
  app.get("*", (_req, res) => {
    res.sendFile(join(WEB_DIST, "index.html"));
  });
}

async function main() {
  await startAdbServer();
  app.listen(PORT, () => {
    console.log(`Slate companion bridge running at http://localhost:${PORT}`);
    if (!existsSync(WEB_DIST)) {
      console.log("Web UI not built — run `npm run dev` from companion/ for the full UI.");
    }
  });
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
