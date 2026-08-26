import { execSync } from "child_process";
import fs from "fs";
import path from "path";
import readline from "readline";

const root = process.cwd();
const overridesDir = path.join(root, "overrides");
const webCoreDir = path.join(root, "web-core");
const branch = "main";

function ask(question) {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });
  return new Promise((resolve) => {
    rl.question(question, (answer) => {
      rl.close();
      resolve(answer.trim().toLowerCase());
    });
  });
}

function collectOverrideFiles() {
  const files = [];
  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(full);
      else if (entry.name !== ".gitkeep")
        files.push(path.relative(overridesDir, full));
    }
  };
  walk(overridesDir);
  return files;
}

async function main() {
  const currentSha = execSync("git rev-parse HEAD", { cwd: webCoreDir })
    .toString()
    .trim();

  process.stdout.write(`Fetching web-core origin/${branch}... `);
  execSync(`git fetch origin ${branch}`, { cwd: webCoreDir, stdio: "pipe" });
  const remoteSha = execSync(`git rev-parse origin/${branch}`, {
    cwd: webCoreDir,
  })
    .toString()
    .trim();
  console.log("done.");

  if (currentSha === remoteSha) {
    console.log(`web-core is already up to date on ${branch}, nothing to do.`);
    return;
  }

  const overrideFiles = collectOverrideFiles();
  const affected = overrideFiles.filter((f) => {
    const out = execSync(
      `git diff --name-only ${currentSha} ${remoteSha} -- "${f}"`,
      { cwd: webCoreDir }
    )
      .toString()
      .trim();
    return out.length > 0;
  });

  if (affected.length > 0) {
    console.log("\nThe following overridden files changed in web-core:\n");
    affected.forEach((f) => console.log(`  web-core/${f}  →  overrides/${f}`));
    console.log(`\nReview the diff before updating:`);
    console.log(
      `  https://github.com/King-Tajin/Vagudle/compare/${currentSha}...${remoteSha}\n`
    );

    const answer = await ask(
      "Update web-core anyway despite these conflicts? (Y/n): "
    );
    if (answer === "n" || answer === "no") {
      console.log("Update cancelled.");
      return;
    }
  } else {
    console.log("No overridden files changed upstream. Safe to update.\n");
  }

  console.log(`Updating web-core to origin/${branch}...`);
  execSync(`git checkout -B ${branch} origin/${branch}`, {
    cwd: webCoreDir,
    stdio: "inherit",
  });
  console.log(
    "web-core updated. Remember to commit the new submodule pointer."
  );
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
