import { settingsKey } from "../lib/localStorage";
import {
  detectBrowserLanguage,
  isSupportedLanguage,
  type Language,
} from "./languages";

const readStoredLanguage = (): Language | null => {
  try {
    const stored = localStorage.getItem(settingsKey);
    if (!stored) return null;
    const parsed = JSON.parse(stored) as { language?: unknown };
    if (
      typeof parsed.language === "string" &&
      isSupportedLanguage(parsed.language)
    ) {
      return parsed.language;
    }
    return null;
  } catch {
    return null;
  }
};

const resolveActiveLanguage = (): Language =>
  readStoredLanguage() ?? detectBrowserLanguage() ?? "en";

type BackgroundTextEntry = {
  desktopLabel: string;
  mobileLabel: string;
  attribution: {
    credits: {
      role: string;
      title: string;
      creator: string;
      sourceUrl?: string;
    }[];
    license: string;
  };
};

const BACKGROUND_TEXT_MOUSE_EATING_BY_LANGUAGE: Record<
  Language,
  BackgroundTextEntry
> = {
  en: {
    desktopLabel: "MOUSE EATING M&M",
    mobileLabel: "MOUSE",
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
  sv: {
    desktopLabel: "MUS SOM ÄTER M&M",
    mobileLabel: "MUS",
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
          role: "Musik",
          title: "Candyland",
          creator: "Tobu",
        },
      ],
      license: "Allmän egendom",
    },
  },
};

const active =
  BACKGROUND_TEXT_MOUSE_EATING_BY_LANGUAGE[resolveActiveLanguage()];

export const BACKGROUND_TEXT_MOUSE_EATING: BackgroundTextEntry = active;
