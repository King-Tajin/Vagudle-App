import { COMPLETIONIST_ID } from "./achievements";
import {
  migrateLegacyStorageKey,
  stampUpdatedAt,
  clearUpdatedAt,
  cloudSyncKey,
} from "./localStorage";
import strings from "../constants/strings";
import { BACKGROUND_TEXT_MOUSE_EATING } from "../constants/extraStrings";

export type BackgroundId =
  | "sprinkles"
  | "flakes"
  | "tnt_rain"
  | "pulsing_purple"
  | "carrots"
  | "flying_mudskipper"
  | "escalating_fire"
  | "dvd_screensaver"
  | "number_rain"
  | "seven_letters"
  | "snowfall"
  | "letter_pile"
  | "letter_rain"
  | "duck_parade"
  | "mouse_eating"
  | "emoji_rain"
  | "fireworks"
  | "liquid_ripple"
  | "spinning_seal";

export type AttributionCredit = {
  role: string;
  title: string;
  creator: string;
  sourceUrl?: string;
};

export type BackgroundAttribution = {
  credits: AttributionCredit[];
  license: string;
};

export type BackgroundDef = {
  id: BackgroundId;
  desktopLabel: string;
  mobileLabel: string;
  requiresAchievementId?: string;
  kind: "css" | "video";
  videoSrc?: string;
  objectPosition?: string;
  attribution?: BackgroundAttribution;
  requiresWebGL?: boolean;
};

export const BACKGROUNDS: BackgroundDef[] = [
  {
    id: "sprinkles",
    ...strings.BACKGROUND_TEXT.sprinkles,
    kind: "css",
  },
  {
    id: "flakes",
    ...strings.BACKGROUND_TEXT.flakes,
    kind: "css",
  },
  {
    id: "tnt_rain",
    ...strings.BACKGROUND_TEXT.tnt_rain,
    requiresAchievementId: "first_win",
    kind: "css",
  },
  {
    id: "pulsing_purple",
    ...strings.BACKGROUND_TEXT.pulsing_purple,
    requiresAchievementId: "win_15",
    kind: "css",
  },
  {
    id: "carrots",
    ...strings.BACKGROUND_TEXT.carrots,
    requiresAchievementId: "win_50",
    kind: "css",
  },
  {
    id: "flying_mudskipper",
    ...strings.BACKGROUND_TEXT.flying_mudskipper,
    requiresAchievementId: "on_a_roll",
    kind: "css",
  },
  {
    id: "escalating_fire",
    ...strings.BACKGROUND_TEXT.escalating_fire,
    requiresAchievementId: "unstoppable",
    kind: "css",
  },
  {
    id: "dvd_screensaver",
    ...strings.BACKGROUND_TEXT.dvd_screensaver,
    requiresAchievementId: "hard_5plus",
    kind: "css",
  },
  {
    id: "number_rain",
    ...strings.BACKGROUND_TEXT.number_rain,
    requiresAchievementId: "fifth_guess",
    kind: "video",
    videoSrc: "/backgrounds/number_rain.mp4",
  },
  {
    id: "seven_letters",
    ...strings.BACKGROUND_TEXT.seven_letters,
    requiresAchievementId: "seven_letters",
    kind: "css",
  },
  {
    id: "snowfall",
    ...strings.BACKGROUND_TEXT.snowfall,
    requiresAchievementId: "close_but_no_cigar",
    kind: "css",
  },
  {
    id: "letter_pile",
    ...strings.BACKGROUND_TEXT.letter_pile,
    requiresAchievementId: "process_of_elimination",
    kind: "css",
  },
  {
    id: "letter_rain",
    ...strings.BACKGROUND_TEXT.letter_rain,
    requiresAchievementId: "word_connoisseur",
    kind: "css",
  },
  {
    id: "duck_parade",
    ...strings.BACKGROUND_TEXT.duck_parade,
    requiresAchievementId: "quack",
    kind: "css",
  },
  {
    id: "mouse_eating",
    ...BACKGROUND_TEXT_MOUSE_EATING,
    requiresAchievementId: "guess_mouse",
    kind: "video",
    videoSrc: "/backgrounds/mouse_v3.mp4",
    objectPosition: "85% 98%",
  },
  {
    id: "emoji_rain",
    ...strings.BACKGROUND_TEXT.emoji_rain,
    requiresAchievementId: "nail_biter",
    kind: "css",
  },
  {
    id: "fireworks",
    ...strings.BACKGROUND_TEXT.fireworks,
    requiresAchievementId: "diversify",
    kind: "css",
  },
  {
    id: "liquid_ripple",
    ...strings.BACKGROUND_TEXT.liquid_ripple,
    requiresAchievementId: "blind_faith",
    kind: "css",
    requiresWebGL: true,
  },
  {
    id: "spinning_seal",
    ...strings.BACKGROUND_TEXT.spinning_seal,
    requiresAchievementId: COMPLETIONIST_ID,
    kind: "video",
    videoSrc: "/backgrounds/seal_v2.mp4",
  },
];

export const BG_KEY = "vagudle-bg-theme:v1";
const LEGACY_BG_KEY = "vagudle-bg-theme";

export const loadBackgroundId = (
  isMobile: boolean,
  isDiscordActivity = false
): BackgroundId => {
  migrateLegacyStorageKey(LEGACY_BG_KEY, BG_KEY);
  try {
    const stored = localStorage.getItem(BG_KEY);
    if (stored && BACKGROUNDS.some((b) => b.id === stored))
      return stored as BackgroundId;
  } catch {}
  if (isDiscordActivity) return "liquid_ripple";
  return isMobile ? "flakes" : "sprinkles";
};

export const saveBackgroundId = (id: BackgroundId): void => {
  try {
    localStorage.setItem(BG_KEY, id);
    stampUpdatedAt(cloudSyncKey);
  } catch {}
};

export const ATTRIBUTION_HIDDEN_KEY = "vagudle-attribution-hidden:v1";
const LEGACY_ATTRIBUTION_HIDDEN_KEY = "vagudle-attribution-hidden";

export const loadHiddenAttributionIds = (): BackgroundId[] => {
  migrateLegacyStorageKey(
    LEGACY_ATTRIBUTION_HIDDEN_KEY,
    ATTRIBUTION_HIDDEN_KEY
  );
  try {
    const stored = localStorage.getItem(ATTRIBUTION_HIDDEN_KEY);
    if (!stored) return [];
    const parsed: unknown = JSON.parse(stored);
    if (Array.isArray(parsed)) return parsed as BackgroundId[];
  } catch {}
  return [];
};

export const hideAttributionForever = (id: BackgroundId): void => {
  try {
    const hidden = loadHiddenAttributionIds();
    if (!hidden.includes(id)) {
      localStorage.setItem(
        ATTRIBUTION_HIDDEN_KEY,
        JSON.stringify([...hidden, id])
      );
      stampUpdatedAt(ATTRIBUTION_HIDDEN_KEY);
    }
  } catch {}
};

export const unhideAttribution = (id: BackgroundId): void => {
  try {
    const hidden = loadHiddenAttributionIds();
    localStorage.setItem(
      ATTRIBUTION_HIDDEN_KEY,
      JSON.stringify(hidden.filter((hiddenId) => hiddenId !== id))
    );
    stampUpdatedAt(ATTRIBUTION_HIDDEN_KEY);
  } catch {}
};

export const clearHiddenAttributions = (): void => {
  try {
    localStorage.removeItem(ATTRIBUTION_HIDDEN_KEY);
    clearUpdatedAt(ATTRIBUTION_HIDDEN_KEY);
  } catch {}
};
