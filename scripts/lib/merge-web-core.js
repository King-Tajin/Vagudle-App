import { execSync } from "child_process";
import fs from "fs";
import path from "path";

const root = process.cwd();
const tmp = path.join(root, ".web-core-tmp");
const webCore = path.join(root, "web-core");
const overrides = path.join(root, "overrides");

const SYNC_EXCLUDES = new Set([
  "node_modules",
  ".git",
  "dist",
  ".wrangler",
  "worker-configuration.d.ts",
  ".eslintcache",
]);

function run(cmd, cwd = root) {
  execSync(cmd, { cwd, stdio: "inherit" });
}

function syncDir(src, dest) {
  fs.mkdirSync(dest, { recursive: true });

  const srcEntries = new Map(
    fs.readdirSync(src, { withFileTypes: true }).map((e) => [e.name, e])
  );

  for (const name of fs.readdirSync(dest)) {
    if (SYNC_EXCLUDES.has(name)) continue;
    if (!srcEntries.has(name)) {
      fs.rmSync(path.join(dest, name), { recursive: true, force: true });
    }
  }

  for (const [name, entry] of srcEntries) {
    if (SYNC_EXCLUDES.has(name)) continue;
    const srcPath = path.join(src, name);
    const destPath = path.join(dest, name);
    if (entry.isDirectory()) {
      syncDir(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

export function mergeWebCore() {
  syncDir(webCore, tmp);

  if (fs.existsSync(overrides)) {
    fs.cpSync(overrides, tmp, { recursive: true, force: true });
  }

  run("pnpm install", tmp);

  return tmp;
}

export function runInMergedTree(script) {
  const tmp = mergeWebCore();
  run(`pnpm run ${script}`, tmp);
}
