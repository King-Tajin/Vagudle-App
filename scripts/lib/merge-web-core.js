import { execSync } from "child_process";
import fs from "fs";
import path from "path";

const root = process.cwd();
const tmp = path.join(root, ".web-tmp");
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

const SERVER_ONLY = ["functions", "migrations", "wrangler.toml", "workers"];

const INSET_ENV_PATTERN =
  /env\(\s*safe-area-inset-(top|right|bottom|left)\s*(?:,[^)]*)?\)/g;
const SOURCE_EXTENSIONS = new Set([".ts", ".tsx", ".css"]);
const REDUNDANT_INSET_CLASSES = [
  {
    file: path.join("src", "App.tsx"),
    text: " pb-[env(safe-area-inset-bottom)]",
  },
];

function collectSourceFiles(dir) {
  const files = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) files.push(...collectSourceFiles(full));
    else if (SOURCE_EXTENSIONS.has(path.extname(entry.name))) files.push(full);
  }
  return files;
}

export function applyInsetTransforms(treeRoot) {
  for (const { file, text } of REDUNDANT_INSET_CLASSES) {
    const target = path.join(treeRoot, file);
    const source = String(fs.readFileSync(target, "utf8"));
    if (!source.includes(text)) {
      throw new Error(
        `Expected "${text}" in web-core/${file} but it was not found. Review the upstream change and update REDUNDANT_INSET_CLASSES in scripts/lib/merge-web-core.js.`
      );
    }
    fs.writeFileSync(target, source.split(text).join(""));
  }

  for (const file of collectSourceFiles(path.join(treeRoot, "src"))) {
    const source = String(fs.readFileSync(file, "utf8"));
    const updated = source.replace(
      INSET_ENV_PATTERN,
      (_match, edge) => `var(--app-inset-${edge})`
    );
    if (updated !== source) fs.writeFileSync(file, updated);
  }
}

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

  applyInsetTransforms(tmp);

  for (const entry of SERVER_ONLY) {
    fs.rmSync(path.join(tmp, entry), { recursive: true, force: true });
  }

  run("pnpm install", tmp);

  return tmp;
}

export function runInMergedTree(script) {
  const tmp = mergeWebCore();
  run(`pnpm run ${script}`, tmp);
}
