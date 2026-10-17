import { Capacitor } from "@capacitor/core";
import { Network } from "@capacitor/network";
import {
  ACHIEVEMENTS_KEY,
  getEffectiveUnlockedIds,
  loadAchievementProgress,
} from "./achievements";
import { prefetchVideo } from "./assetPacks";
import { BACKGROUNDS } from "./backgrounds";

let started = false;

const readUnlockedIds = (): Set<string> =>
  new Set(getEffectiveUnlockedIds(loadAchievementProgress().unlockedIds));

const canPrefetch = async (): Promise<boolean> => {
  try {
    const { connectionType } = await Network.getStatus();
    return connectionType !== "cellular" && connectionType !== "none";
  } catch {
    return false;
  }
};

const prefetchUnlockedVideos = async (newIds: string[]): Promise<void> => {
  const sources = BACKGROUNDS.filter(
    (bg) =>
      bg.kind === "video" &&
      bg.videoSrc !== undefined &&
      bg.requiresAchievementId !== undefined &&
      newIds.includes(bg.requiresAchievementId)
  ).map((bg) => bg.videoSrc as string);

  if (sources.length === 0 || !(await canPrefetch())) return;
  await Promise.all(sources.map((src) => prefetchVideo(src)));
};

export const initAssetPackPrefetch = (): void => {
  if (started || !Capacitor.isNativePlatform()) return;
  started = true;

  let knownIds = readUnlockedIds();

  const handleAchievementsChanged = () => {
    const currentIds = readUnlockedIds();
    const newIds = [...currentIds].filter((id) => !knownIds.has(id));
    knownIds = currentIds;
    if (newIds.length > 0) void prefetchUnlockedVideos(newIds);
  };

  const originalSetItem = Storage.prototype.setItem;
  Storage.prototype.setItem = function (
    this: Storage,
    key: string,
    value: string
  ) {
    originalSetItem.call(this, key, value);
    if (key === ACHIEVEMENTS_KEY && this === window.localStorage) {
      queueMicrotask(handleAchievementsChanged);
    }
  };
};
