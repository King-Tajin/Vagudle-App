// noinspection JSUnresolvedReference

import { spawnSync } from "node:child_process";
import { join } from "node:path";

const tasks = process.argv.slice(2);

if (tasks.length === 0) {
  console.error("Usage: node scripts/gradle.js <task> [...more tasks]");
  process.exit(1);
}

const androidDir = join(process.cwd(), "android");
const wrapper = process.platform === "win32" ? "gradlew.bat" : "./gradlew";

const result = spawnSync(wrapper, tasks, {
  cwd: androidDir,
  stdio: "inherit",
  shell: true,
});

if (result.error) {
  console.error(result.error);
  process.exit(1);
}

process.exit(result.status ?? 1);
