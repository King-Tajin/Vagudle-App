import { execSync, spawn } from "child_process";
import fs from "fs";
import path from "path";
import { mergeWebCore } from "./lib/merge-web-core.js";

const root = process.cwd();
const dist = path.join(root, "dist");
const VITE_BUILD_COMPLETE_MARKER = /✓ built in/;
const VITE_BUILD_MAX_MS = 5 * 60 * 1000;
const VITE_BUILD_EXIT_GRACE_MS = 20 * 1000;

function run(cmd, cwd = root) {
  execSync(cmd, { cwd, stdio: "inherit" });
}

function copyDir(src, dest) {
  fs.cpSync(src, dest, { recursive: true, force: true });
}

function killTree(pid) {
  if (process.platform === "win32") {
    execSync(`taskkill /pid ${pid} /T /F`, { stdio: "ignore" });
  } else {
    process.kill(-pid, "SIGKILL");
  }
}

function runViteBuild(cwd) {
  return new Promise((resolve, reject) => {
    const child = spawn("pnpm", ["exec", "vite", "build"], {
      cwd,
      shell: true,
      stdio: ["ignore", "pipe", "pipe"],
      detached: process.platform !== "win32",
    });

    let output = "";
    let built = false;
    let settled = false;
    let exitGraceTimer = null;

    const finish = (err) => {
      if (settled) return;
      settled = true;
      clearTimeout(maxTimer);
      if (exitGraceTimer) clearTimeout(exitGraceTimer);
      if (err) reject(err);
      else resolve();
    };

    const onStdout = (chunk) => {
      process.stdout.write(chunk);
      output += chunk.toString();
      if (!built && VITE_BUILD_COMPLETE_MARKER.test(output)) {
        built = true;
        exitGraceTimer = setTimeout(() => {
          console.warn(
              `vite build finished but its process did not exit on its own; force-killed the process tree after ${VITE_BUILD_EXIT_GRACE_MS / 1000}s and continuing.`
          );
          killTree(child.pid);
        }, VITE_BUILD_EXIT_GRACE_MS);
      }
    };

    child.stdout.on("data", onStdout);
    child.stderr.on("data", (chunk) => process.stderr.write(chunk));

    const maxTimer = setTimeout(() => {
      killTree(child.pid);
      finish(new Error(`vite build did not complete within ${VITE_BUILD_MAX_MS / 1000}s`));
    }, VITE_BUILD_MAX_MS);

    child.on("exit", (code) => {
      if (built || code === 0) {
        finish();
        return;
      }
      finish(new Error(`vite build exited with code ${code}`));
    });

    child.on("error", finish);
  });
}

fs.rmSync(dist, { recursive: true, force: true });

const tmp = mergeWebCore();
fs.rmSync(path.join(tmp, "dist"), { recursive: true, force: true });

await runViteBuild(tmp);
run("node scripts/optimize-dist.mjs", tmp);

copyDir(path.join(tmp, "dist"), dist);

run("pnpm exec cap sync");
