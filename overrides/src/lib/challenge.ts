export type ChallengeDict = "normal" | "hard" | "full";

export type ChallengeConfig = {
  word: string;
  dict: ChallengeDict;
  guesses: 9 | 11;
  id: string;
  length: number;
};

export type ChallengeGameState = {
  guesses: string[];
  cellColors: { [key: string]: string };
  autoGrayLetters: string[];
  savedAt?: number;
};

const TWO_YEARS_MS = 2.5 * 365 * 24 * 60 * 60 * 1000;
const MAX_CHALLENGE_ENTRIES = 975;

export const pruneOldChallengeStates = (): void => {
  const now = Date.now();
  const surviving: { key: string; savedAt: number }[] = [];

  const allKeys: string[] = [];
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key?.startsWith("chal_")) allKeys.push(key);
  }

  for (const key of allKeys) {
    try {
      const stored = localStorage.getItem(key);
      if (!stored) {
        localStorage.removeItem(key);
        continue;
      }
      const parsed = JSON.parse(stored) as ChallengeGameState;
      if (!parsed.savedAt || now - parsed.savedAt > TWO_YEARS_MS) {
        localStorage.removeItem(key);
      } else {
        surviving.push({ key, savedAt: parsed.savedAt });
      }
    } catch {
      localStorage.removeItem(key);
    }
  }

  if (surviving.length > MAX_CHALLENGE_ENTRIES) {
    surviving
      .sort((a, b) => a.savedAt - b.savedAt)
      .slice(0, surviving.length - MAX_CHALLENGE_ENTRIES)
      .forEach(({ key }) => localStorage.removeItem(key));
  }
};

export const encodeChallenge = async (
  config: Omit<ChallengeConfig, "id">
): Promise<{ encoded: string; id: string } | null> => {
  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 10000);
    const res = await fetch("/api/challenge", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(config),
      signal: controller.signal,
    });
    clearTimeout(timeout);
    if (!res.ok) return null;
    const data = (await res.json()) as {
      success: boolean;
      encoded: string;
      id: string;
    };
    if (!data.success) return null;
    return { encoded: data.encoded, id: data.id };
  } catch {
    return null;
  }
};

export const decodeChallenge = async (
  token: string
): Promise<ChallengeConfig | null> => {
  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 10000);
    const res = await fetch(
      `/api/challenge?token=${encodeURIComponent(token)}`,
      {
        signal: controller.signal,
      }
    );
    clearTimeout(timeout);
    if (!res.ok) return null;
    const data = (await res.json()) as {
      success: boolean;
      config: ChallengeConfig;
    };
    if (!data.success) return null;
    return data.config;
  } catch {
    return null;
  }
};

export const buildChallengeUrl = (encoded: string): string =>
  `https://vagudle.king-tajin.dev/?challenge=${encoded}`;

export const challengeStateKey = (id: string) => `chal_${id}`;

export const saveChallengeState = (
  id: string,
  state: ChallengeGameState
): void => {
  try {
    localStorage.setItem(
      challengeStateKey(id),
      JSON.stringify({ ...state, savedAt: Date.now() })
    );
  } catch {}
};

export const loadChallengeState = (id: string): ChallengeGameState | null => {
  try {
    const stored = localStorage.getItem(challengeStateKey(id));
    if (!stored) return null;
    const parsed = JSON.parse(stored) as ChallengeGameState;
    if (!parsed.savedAt || Date.now() - parsed.savedAt > TWO_YEARS_MS) {
      localStorage.removeItem(challengeStateKey(id));
      return null;
    }
    return parsed;
  } catch {
    localStorage.removeItem(challengeStateKey(id));
    return null;
  }
};

export const DICT_LABELS: Record<ChallengeDict, string> = {
  normal: "Normal",
  hard: "Hard",
  full: "Extreme",
};

export const DICT_DESCRIPTIONS: Record<ChallengeDict, string> = {
  normal: "Common English words",
  hard: "Uncommon English words",
  full: "Full Scrabble dictionary",
};
