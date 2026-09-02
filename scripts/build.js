import { execSync } from "child_process";
import fs from "fs";
import path from "path";
import { mergeWebCore } from "./lib/merge-web-core.js";

const root = process.cwd();
const dist = path.join(root, "dist");

function run(cmd, cwd = root) {
  execSync(cmd, { cwd, stdio: "inherit" });
}

function copyDir(src, dest) {
  fs.cpSync(src, dest, { recursive: true, force: true });
}

fs.rmSync(dist, { recursive: true, force: true });

const tmp = mergeWebCore();
fs.rmSync(path.join(tmp, "dist"), { recursive: true, force: true });

run("pnpm exec vite build", tmp);
run("node scripts/optimize-dist.mjs", tmp);

copyDir(path.join(tmp, "dist"), dist);

run("pnpm exec cap sync");