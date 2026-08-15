import { execSync } from "child_process";
import fs from "fs";
import path from "path";

const root = process.cwd();
const tmp = path.join(root, ".build-tmp");
const webCore = path.join(root, "web-core");
const overrides = path.join(root, "overrides");
const dist = path.join(root, "dist");

const SERVER_ONLY = ["functions", "migrations", "wrangler.toml"];

function run(cmd, cwd = root) {
  execSync(cmd, { cwd, stdio: "inherit" });
}

function copyDir(src, dest) {
  fs.cpSync(src, dest, { recursive: true, force: true });
}

fs.rmSync(tmp, { recursive: true, force: true });
fs.rmSync(dist, { recursive: true, force: true });

copyDir(webCore, tmp);

if (fs.existsSync(overrides)) {
  copyDir(overrides, tmp);
}

for (const entry of SERVER_ONLY) {
  fs.rmSync(path.join(tmp, entry), { recursive: true, force: true });
}

run("pnpm install", tmp);
run("pnpm run build", tmp);

copyDir(path.join(tmp, "dist"), dist);

run("pnpm exec cap sync");

fs.rmSync(tmp, { recursive: true, force: true });
