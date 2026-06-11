import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';

const root = process.cwd();
const overridesDir = path.join(root, 'overrides');
const webCoreDir = path.join(root, 'web-core');

const currentSha = execSync('git rev-parse HEAD', { cwd: webCoreDir }).toString().trim();

process.stdout.write('Fetching web-core remote... ');
execSync('git fetch origin', { cwd: webCoreDir, stdio: 'pipe' });
const remoteSha = execSync('git rev-parse origin/HEAD', { cwd: webCoreDir }).toString().trim();
console.log('done.');

if (currentSha === remoteSha) {
    console.log('web-core is already up to date, nothing to check.');
    process.exit(0);
}

const overrideFiles = [];
const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        const full = path.join(dir, entry.name);
        if (entry.isDirectory()) walk(full);
        else if (entry.name !== '.gitkeep') overrideFiles.push(path.relative(overridesDir, full));
    }
};
walk(overridesDir);

const affected = overrideFiles.filter((f) => {
    const out = execSync(`git diff --name-only ${currentSha} ${remoteSha} -- "${f}"`, {
        cwd: webCoreDir,
    }).toString().trim();
    return out.length > 0;
});

if (affected.length === 0) {
    console.log('No overridden files changed upstream. Safe to update.\n');
    process.exit(0);
}

console.log('\nThe following overridden files changed in web-core:\n');
affected.forEach((f) => console.log(`  web-core/${f}  →  overrides/${f}`));
console.log(`\nReview the diff before updating:`);
console.log(`  https://github.com/King-Tajin/Vagudle/compare/${currentSha}...${remoteSha}\n`);
console.log('Update the overrides if needed, then run the update manually:');
console.log('  git submodule update --remote web-core && git add web-core && git commit -m \'chore: update web-core\'\n');
process.exit(1);
