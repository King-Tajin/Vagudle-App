import { COMPLETIONIST_ID } from "./achievements";
import {
  migrateLegacyStorageKey,
  stampUpdatedAt,
  clearUpdatedAt,
  cloudSyncKey,
} from "./localStorage";

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
    desktopLabel: "VAGUDLE SPRINKLES",
    mobileLabel: "GRAY",
    kind: "css",
  },
  {
    id: "flakes",
    desktopLabel: "FLAKE RAIN",
    mobileLabel: "GRID",
    kind: "css",
  },
  {
    id: "tnt_rain",
    desktopLabel: "TNT RAIN",
    mobileLabel: "TNT",
    requiresAchievementId: "first_win",
    kind: "css",
  },
  {
    id: "pulsing_purple",
    desktopLabel: "PULSING PURPLE",
    mobileLabel: "PURPLE",
    requiresAchievementId: "win_15",
    kind: "css",
  },
  {
    id: "carrots",
    desktopLabel: "SPINNING CARROTS",
    mobileLabel: "CARROTS",
    requiresAchievementId: "win_50",
    kind: "css",
  },
  {
    id: "flying_mudskipper",
    desktopLabel: "FLYING MUDSKIPPER",
    mobileLabel: "MUDSKIPPER",
    requiresAchievementId: "on_a_roll",
    kind: "css",
  },
  {
    id: "escalating_fire",
    desktopLabel: "ESCALATING FIRE",
    mobileLabel: "FIRE",
    requiresAchievementId: "unstoppable",
    kind: "css",
  },
  {
    id: "dvd_screensaver",
    desktopLabel: "DVD SCREENSAVER",
    mobileLabel: "DVD",
    requiresAchievementId: "hard_5plus",
    kind: "css",
  },
  {
    id: "number_rain",
    desktopLabel: "NUMBER RAIN",
    mobileLabel: "NUMBERS",
    requiresAchievementId: "fifth_guess",
    kind: "video",
    videoSrc: "/backgrounds/number_rain.mp4",
    attribution: {
      credits: [
        {
          role: "Video",
          title: "Matrix Rain Codes (4K FULL HD)",
          creator: "Fatih Kalkan",
          sourceUrl: "https://www.youtube.com/watch?v=MUVo20q6tx8",
        },
      ],
      license: "Creative Commons Attribution license (reuse allowed)",
    },
  },
  {
    id: "seven_letters",
    desktopLabel: "SEVEN LETTER WORDS",
    mobileLabel: "WORDS",
    requiresAchievementId: "seven_letters",
    kind: "css",
  },
  {
    id: "snowfall",
    desktopLabel: "SNOWFALL",
    mobileLabel: "SNOW",
    requiresAchievementId: "close_but_no_cigar",
    kind: "css",
  },
  {
    id: "letter_pile",
    desktopLabel: "LETTER PILE",
    mobileLabel: "PILE",
    requiresAchievementId: "process_of_elimination",
    kind: "css",
  },
  {
    id: "letter_rain",
    desktopLabel: "LETTER RAIN",
    mobileLabel: "LETTERS",
    requiresAchievementId: "word_connoisseur",
    kind: "css",
  },
  {
    id: "duck_parade",
    desktopLabel: "DUCK PARADE",
    mobileLabel: "DUCKS",
    requiresAchievementId: "quack",
    kind: "css",
  },
  {
    id: "mouse_eating",
    desktopLabel: "MOUSE EATING M&M",
    mobileLabel: "MOUSE",
    requiresAchievementId: "guess_mouse",
    kind: "video",
    videoSrc: "/backgrounds/mouse_v3.mp4",
    objectPosition: "85% 98%",
    attribution: {
      credits: [
        {
          role: "Video",
          title:
            "3d cartoon mouse dancing loop animation isolated on green screen background Free Video",
          creator: "Vecteezy",
          sourceUrl:
            "https://www.vecteezy.com/video/56169680-3d-cartoon-mouse-dancing-loop-animation-isolated-on-green-screen-background",
        },
        {
          role: "Music",
          title: "Candyland",
          creator: "Tobu",
        },
      ],
      license: "Public Domain",
    },
  },
  {
    id: "emoji_rain",
    desktopLabel: "EMOJI RAIN",
    mobileLabel: "EMOJIS",
    requiresAchievementId: "nail_biter",
    kind: "css",
  },
  {
    id: "fireworks",
    desktopLabel: "FIREWORKS",
    mobileLabel: "FIREWORKS",
    requiresAchievementId: "diversify",
    kind: "css",
  },
  {
    id: "liquid_ripple",
    desktopLabel: "LIQUID RIPPLES",
    mobileLabel: "RIPPLES",
    requiresAchievementId: "blind_faith",
    kind: "css",
    requiresWebGL: true,
  },
  {
    id: "spinning_seal",
    desktopLabel: "SPINNING SEAL",
    mobileLabel: "SEAL",
    requiresAchievementId: COMPLETIONIST_ID,
    kind: "video",
    videoSrc: "/backgrounds/seal_v2.mp4",
    attribution: {
      credits: [
        {
          role: "Video",
          title: "there is no need to be upset",
          creator: "High Valley",
          sourceUrl: "https://www.youtube.com/watch?v=GJDNkVDGM_s&t=14s",
        },
        {
          role: "Music",
          title: "Happy H. Christmas",
          creator: "Maniacs of Noise",
        },
      ],
      license: "Creative Commons Attribution (CC BY)",
    },
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
