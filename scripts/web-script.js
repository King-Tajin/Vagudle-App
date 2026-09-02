import { runInMergedTree } from "./lib/merge-web-core.js";

const script = process.argv[2];

if (!script) {
  console.error("Usage: node scripts/web-script.js <script-name>");
  process.exit(1);
}

runInMergedTree(script);
