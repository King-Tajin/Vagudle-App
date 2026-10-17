import { Capacitor, registerPlugin } from "@capacitor/core";
import type { PluginListenerHandle } from "@capacitor/core";

type PackStatus =
  | "pending"
  | "downloading"
  | "transferring"
  | "completed"
  | "failed"
  | "canceled"
  | "waiting_for_wifi"
  | "not_installed"
  | "requires_user_confirmation"
  | "unknown";

type PackState = {
  name: string;
  status: PackStatus;
  bytesDownloaded: number;
  totalBytes: number;
  percent: number;
  errorCode: number;
};

type AssetPacksPlugin = {
  getLocation: (options: { name: string }) => Promise<{ path?: string | null }>;
  fetch: (options: { name: string }) => Promise<PackState>;
  addListener: (
    eventName: "packState",
    listenerFunc: (state: PackState) => void
  ) => Promise<PluginListenerHandle>;
};

type PackManifestEntry = {
  pack: string;
  file: string;
};

type PackManifest = Record<string, PackManifestEntry>;

export type PackProgress = {
  received: number;
  total: number;
};

const MANIFEST_URL = "/asset-packs.json";
const FAILED_STATUSES = new Set<PackStatus>([
  "failed",
  "canceled",
  "not_installed",
  "requires_user_confirmation",
]);

const AssetPacks = registerPlugin<AssetPacksPlugin>("AssetPacks");

let manifestPromise: Promise<PackManifest> | null = null;
const prefetching = new Set<string>();

const isManifestEntry = (value: unknown): value is PackManifestEntry =>
  typeof value === "object" &&
  value !== null &&
  typeof (value as PackManifestEntry).pack === "string" &&
  typeof (value as PackManifestEntry).file === "string";

const readManifest = async (): Promise<PackManifest> => {
  try {
    const response = await fetch(MANIFEST_URL);
    if (!response.ok) return {};
    const data: unknown = await response.json();
    const packs = (data as { packs?: unknown } | null)?.packs;
    if (typeof packs !== "object" || packs === null) return {};

    const manifest: PackManifest = {};
    for (const [webPath, entry] of Object.entries(packs)) {
      if (isManifestEntry(entry)) manifest[webPath] = entry;
    }
    return manifest;
  } catch {
    return {};
  }
};

const loadManifest = (): Promise<PackManifest> => {
  manifestPromise ??= readManifest();
  return manifestPromise;
};

const waitForPack = async (
  name: string,
  onProgress: (progress: PackProgress) => void,
  signal: AbortSignal
): Promise<string | null> => {
  let finish: (path: string | null) => void = () => {};
  const done = new Promise<string | null>((resolve) => {
    finish = resolve;
  });

  const handleState = async (state: PackState): Promise<void> => {
    if (state.name !== name) return;

    if (state.status === "completed") {
      const location = await AssetPacks.getLocation({ name });
      finish(location.path ?? null);
      return;
    }

    if (FAILED_STATUSES.has(state.status)) {
      finish(null);
      return;
    }

    onProgress({ received: state.bytesDownloaded, total: state.totalBytes });
  };

  const onAbort = () => finish(null);
  signal.addEventListener("abort", onAbort, { once: true });

  const handle = await AssetPacks.addListener("packState", (state) => {
    void handleState(state).catch(() => finish(null));
  });

  try {
    if (signal.aborted) return null;
    await handleState(await AssetPacks.fetch({ name }));
    return await done;
  } finally {
    signal.removeEventListener("abort", onAbort);
    await handle.remove();
  }
};

const ensurePack = async (
  name: string,
  onProgress: (progress: PackProgress) => void,
  signal: AbortSignal
): Promise<string | null> => {
  const existing = await AssetPacks.getLocation({ name });
  if (existing.path) return existing.path;
  return waitForPack(name, onProgress, signal);
};

export const resolveVideoUrl = async (
  src: string,
  onProgress: (progress: PackProgress) => void,
  signal: AbortSignal
): Promise<string> => {
  if (!Capacitor.isNativePlatform()) return src;

  const entry = (await loadManifest())[src];
  if (!entry) return src;

  try {
    const packPath = await ensurePack(entry.pack, onProgress, signal);
    if (!packPath) return src;
    return Capacitor.convertFileSrc(`${packPath}/${entry.file}`);
  } catch {
    return src;
  }
};

export const prefetchVideo = async (src: string): Promise<void> => {
  if (!Capacitor.isNativePlatform()) return;

  const entry = (await loadManifest())[src];
  if (!entry || prefetching.has(entry.pack)) return;

  prefetching.add(entry.pack);
  try {
    const existing = await AssetPacks.getLocation({ name: entry.pack });
    if (existing.path) return;
    await AssetPacks.fetch({ name: entry.pack });
  } catch {
  } finally {
    prefetching.delete(entry.pack);
  }
};
