import fs from "fs";
import path from "path";

const root = process.cwd();
const androidDir = path.join(root, "android");

const PACK_PREFIX = "bg_";
const MANIFEST_NAME = "asset-packs.json";
const PACKED_VIDEOS = ["number_rain.mp4", "mouse_v3.mp4", "seal_v3.mp4"];

const packNameFor = (file) =>
  `${PACK_PREFIX}${path
    .parse(file)
    .name.toLowerCase()
    .replace(/[^a-z0-9]+/g, "_")}`;

const packBuildGradle = (packName) => `plugins {
    id 'com.android.asset-pack'
}

assetPack {
    packName = "${packName}"
    dynamicDelivery {
        deliveryType = "on-demand"
    }
}
`;

export function removeAssetPacks() {
  for (const entry of fs.readdirSync(androidDir, { withFileTypes: true })) {
    if (entry.isDirectory() && entry.name.startsWith(PACK_PREFIX)) {
      fs.rmSync(path.join(androidDir, entry.name), {
        recursive: true,
        force: true,
      });
    }
  }
}

export function createAssetPacks(distDir) {
  const backgroundsDir = path.join(distDir, "backgrounds");
  const manifest = { packs: {} };
  let movedBytes = 0;

  for (const file of PACKED_VIDEOS) {
    const source = path.join(backgroundsDir, file);
    if (!fs.existsSync(source)) {
      throw new Error(
        `Asset pack video not found in dist: backgrounds/${file}. Update PACKED_VIDEOS in scripts/lib/asset-packs.js.`
      );
    }

    const packName = packNameFor(file);
    const packDir = path.join(androidDir, packName);
    const assetsDir = path.join(packDir, "src", "main", "assets");

    fs.mkdirSync(assetsDir, { recursive: true });
    fs.writeFileSync(
      path.join(packDir, "build.gradle"),
      packBuildGradle(packName)
    );
    fs.copyFileSync(source, path.join(assetsDir, file));
    movedBytes += fs.statSync(source).size;
    fs.rmSync(source);

    manifest.packs[`/backgrounds/${file}`] = { pack: packName, file };
  }

  fs.writeFileSync(path.join(distDir, MANIFEST_NAME), JSON.stringify(manifest));

  console.log(
    `asset-packs: moved ${PACKED_VIDEOS.length} video(s) (${(movedBytes / 1_048_576).toFixed(1)} MB) out of the base app into on-demand asset packs`
  );
}
