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

run("pnpm run build", tmp);

copyDir(path.join(tmp, "dist"), dist);

run("pnpm exec cap sync");