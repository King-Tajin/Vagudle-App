import { stampUpdatedAt, cloudSyncKey } from "./localStorage";

export type DailyConfig = {
  date: string;
  word: string;
  wordLength: number;
  hardMode: boolean;
  originDate: string;
};

export type DailyResult = {
  date: string;
  won: boolean;
  guessCount: number;
  maxGuesses: number;
  wordLength: number;
  completedAt: number;
  guesses?: string[];
  cellColors?: { [key: string]: string };
};

export type DailyProgress = {
  guesses: string[];
  cellColors: { [key: string]: string };
  savedAt?: number;
};

export type DailyStats = {
  currentStreak: number;
  bestStreak: number;
  totalPlayed: number;
  totalWon: number;
  lastCompletedDate: string | null;
};

const ONE_DAY_MS = 24 * 60 * 60 * 1000;

export const DAILY_RELEASE_HOUR_UTC = 8;

export const DAILY_PATH = "/daily";

export const msUntilNextDailyRelease = (date: Date = new Date()): number => {
  const todayRelease = Date.UTC(
    date.getUTCFullYear(),
    date.getUTCMonth(),
    date.getUTCDate(),
    DAILY_RELEASE_HOUR_UTC
  );
  const next =
    date.getTime() < todayRelease ? todayRelease : todayRelease + ONE_DAY_MS;
  return next - date.getTime();
};

export const getCurrentDailyWeekday = (date: Date = new Date()): number => {
  const shifted = new Date(
    date.getTime() - DAILY_RELEASE_HOUR_UTC * 60 * 60 * 1000
  );
  return shifted.getUTCDay();
};

export const getDailyNumber = (date: string, originDate: string): number => {
  const start = new Date(`${originDate}T00:00:00Z`).getTime();
  const current = new Date(`${date}T00:00:00Z`).getTime();
  return Math.max(1, Math.round((current - start) / ONE_DAY_MS) + 1);
};

export const fetchDailyConfig = async (): Promise<DailyConfig | null> => {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10000);
  try {
    const res = await fetch("/api/daily", { signal: controller.signal });
    if (!res.ok) return null;
    const data = (await res.json()) as
      | {
          success: true;
          date: string;
          word: string;
          wordLength: number;
          hardMode: boolean;
          originDate: string;
        }
      | { success: false; error: string };
    if (!data.success) return null;
    return {
      date: data.date,
      word: data.word,
      wordLength: data.wordLength,
      hardMode: data.hardMode,
      originDate: data.originDate,
    };
  } catch {
    return null;
  } finally {
    clearTimeout(timeout);
  }
};

export const submitActivityDailyResult = async (
  accessToken: string,
  guesses: string[],
  signal?: AbortSignal
): Promise<boolean> => {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10000);
  const onExternalAbort = () => controller.abort();
  signal?.addEventListener("abort", onExternalAbort);
  try {
    const res = await fetch("/api/activity-daily-result", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        access_token: accessToken,
        guesses,
      }),
      signal: controller.signal,
    });
    if (!res.ok) return false;
    const data = (await res.json()) as { success: boolean };
    return data.success;
  } catch {
    return false;
  } finally {
    clearTimeout(timeout);
    signal?.removeEventListener("abort", onExternalAbort);
  }
};

export const submitActivityDailyGuess = async (
  accessToken: string,
  guess: string,
  guessNumber: number,
  guesses?: string[],
  cellColors?: { [key: string]: string }
): Promise<void> => {
  try {
    await fetch("/api/activity-daily-guess", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        access_token: accessToken,
        guess,
        guess_number: guessNumber,
        guesses,
        cell_colors: cellColors,
      }),
    });
  } catch {
    return;
  }
};

export const saveServerDailyProgress = async (
  idToken: string,
  guesses: string[],
  cellColors: { [key: string]: string }
): Promise<void> => {
  try {
    await fetch("/api/daily-progress", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${idToken}`,
      },
      body: JSON.stringify({ guesses, cellColors }),
    });
  } catch {
    return;
  }
};

export const fetchServerDailyProgress = async (
  idToken: string
): Promise<DailyProgress | null> => {
  try {
    const res = await fetch("/api/daily-progress", {
      headers: { Authorization: `Bearer ${idToken}` },
    });
    if (!res.ok) return null;
    const data = (await res.json()) as {
      success: boolean;
      guesses?: string[] | null;
      cellColors?: { [key: string]: string } | null;
    };
    if (!data.success || !data.guesses) return null;
    return { guesses: data.guesses, cellColors: data.cellColors ?? {} };
  } catch {
    return null;
  }
};

const progressKey = (date: string) => `daily_progress_${date}`;

export const saveDailyProgress = (
  date: string,
  progress: Omit<DailyProgress, "savedAt">
): void => {
  try {
    localStorage.setItem(
      progressKey(date),
      JSON.stringify({ ...progress, savedAt: Date.now() })
    );
  } catch {}
};

export const loadDailyProgress = (date: string): DailyProgress | null => {
  try {
    const stored = localStorage.getItem(progressKey(date));
    return stored ? (JSON.parse(stored) as DailyProgress) : null;
  } catch {
    return null;
  }
};

export const clearDailyProgress = (date: string): void => {
  try {
    localStorage.removeItem(progressKey(date));
  } catch {}
};

const resultKey = (date: string) => `daily_result_${date}`;

export const saveDailyResult = (result: DailyResult): void => {
  try {
    localStorage.setItem(resultKey(result.date), JSON.stringify(result));
  } catch {}
};

export const loadDailyResult = (date: string): DailyResult | null => {
  try {
    const stored = localStorage.getItem(resultKey(date));
    return stored ? (JSON.parse(stored) as DailyResult) : null;
  } catch {
    return null;
  }
};

const DAILY_RETENTION_MS = 3 * ONE_DAY_MS;

export const pruneOldDailyEntries = (): void => {
  const now = Date.now();
  const keysToRemove: string[] = [];

  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (!key) continue;
    if (!key.startsWith("daily_progress_") && !key.startsWith("daily_result_"))
      continue;

    const dateString = key.slice(key.indexOf("_", 6) + 1);
    const parsed = new Date(`${dateString}T00:00:00Z`).getTime();
    if (Number.isNaN(parsed) || now - parsed > DAILY_RETENTION_MS) {
      keysToRemove.push(key);
    }
  }

  keysToRemove.forEach((key) => {
    try {
      localStorage.removeItem(key);
    } catch {}
  });
};

export const dailyStatsKey = "dailyStats:v1";

const defaultDailyStats: DailyStats = {
  currentStreak: 0,
  bestStreak: 0,
  totalPlayed: 0,
  totalWon: 0,
  lastCompletedDate: null,
};

export const loadDailyStats = (): DailyStats => {
  try {
    const stored = localStorage.getItem(dailyStatsKey);
    return stored
      ? { ...defaultDailyStats, ...(JSON.parse(stored) as Partial<DailyStats>) }
      : defaultDailyStats;
  } catch {
    return defaultDailyStats;
  }
};

export const saveDailyStats = (stats: DailyStats): void => {
  try {
    localStorage.setItem(dailyStatsKey, JSON.stringify(stats));
    stampUpdatedAt(cloudSyncKey);
  } catch {}
};

const isDayAfter = (previousDate: string, date: string): boolean => {
  const previous = new Date(`${previousDate}T00:00:00Z`).getTime();
  const current = new Date(`${date}T00:00:00Z`).getTime();
  return current - previous === ONE_DAY_MS;
};

export type DailyLeaderboardEntry = {
  username: string;
  wins: number;
  losses: number;
  currentStreak: number;
  bestStreak: number;
};

export type DailyLeaderboardSelf = DailyLeaderboardEntry & { rank: number };

export type DailyLeaderboardResponse = {
  top: DailyLeaderboardEntry[];
  self: DailyLeaderboardSelf | null;
  page: number;
  totalPages: number;
  totalEntries: number;
  pageSize: number;
};

export type SubmitDailyResultOutcome =
  "recorded" | "already_submitted" | "no_display_name" | "error";

export const submitDailyResult = async (
  idToken: string,
  won: boolean
): Promise<SubmitDailyResultOutcome> => {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10000);
  try {
    const res = await fetch("/api/daily-result", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${idToken}`,
      },
      body: JSON.stringify({ won }),
      signal: controller.signal,
    });
    if (res.status === 409) return "already_submitted";
    if (res.status === 422) return "no_display_name";
    const data = (await res.json()) as { success: boolean };
    return data.success ? "recorded" : "error";
  } catch {
    return "error";
  } finally {
    clearTimeout(timeout);
  }
};

export const fetchDailyLeaderboard = async (
  idToken: string | null,
  page: number = 1
): Promise<DailyLeaderboardResponse | null> => {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10000);
  try {
    const res = await fetch(`/api/daily-leaderboard?page=${page}`, {
      headers: idToken ? { Authorization: `Bearer ${idToken}` } : {},
      signal: controller.signal,
    });
    if (!res.ok) return null;
    const data = (await res.json()) as
      | {
          success: true;
          top: DailyLeaderboardEntry[];
          self: DailyLeaderboardSelf | null;
          page: number;
          totalPages: number;
          totalEntries: number;
          pageSize: number;
        }
      | { success: false; error: string };
    if (!data.success) return null;
    return {
      top: data.top,
      self: data.self,
      page: data.page,
      totalPages: data.totalPages,
      totalEntries: data.totalEntries,
      pageSize: data.pageSize,
    };
  } catch {
    return null;
  } finally {
    clearTimeout(timeout);
  }
};

export type DailyRotationEntry = { wordLength: number; hardMode: boolean };

export const DAILY_SCHEDULE: DailyRotationEntry[] = [
  { wordLength: 4, hardMode: false },
  { wordLength: 4, hardMode: true },
  { wordLength: 5, hardMode: false },
  { wordLength: 5, hardMode: true },
  { wordLength: 4, hardMode: false },
  { wordLength: 5, hardMode: true },
  { wordLength: 4, hardMode: true },
];

export const WEEKDAY_NAMES = [
  "Sunday",
  "Monday",
  "Tuesday",
  "Wednesday",
  "Thursday",
  "Friday",
  "Saturday",
];

export const getLocalDailyUnlockTime = (date: Date = new Date()): string => {
  const releaseTime = new Date(
    Date.UTC(
      date.getUTCFullYear(),
      date.getUTCMonth(),
      date.getUTCDate(),
      DAILY_RELEASE_HOUR_UTC
    )
  );
  return releaseTime.toLocaleTimeString(undefined, {
    hour: "numeric",
    minute: "2-digit",
    timeZoneName: "short",
  });
};

export const DAILY_CALENDAR_FIRST_HOUR_UTC = DAILY_RELEASE_HOUR_UTC;
export const DAILY_CALENDAR_LAST_HOUR_UTC = 23;

export const DAILY_CALENDAR_HOURS: number[] = Array.from(
  { length: DAILY_CALENDAR_LAST_HOUR_UTC - DAILY_CALENDAR_FIRST_HOUR_UTC + 1 },
  (_, i) => DAILY_CALENDAR_FIRST_HOUR_UTC + i
);

export const dailyCalendarFileName = (hourUtc: number): string =>
  `daily-reminder-${String(hourUtc).padStart(2, "0")}.ics`;

export const getDailyCalendarHourLabel = (
  hourUtc: number,
  date: Date = new Date()
): string => {
  const localTime = new Date(
    Date.UTC(
      date.getUTCFullYear(),
      date.getUTCMonth(),
      date.getUTCDate(),
      hourUtc
    )
  );
  const time = localTime.toLocaleTimeString(undefined, {
    hour: "numeric",
    minute: "2-digit",
  });
  return hourUtc === DAILY_RELEASE_HOUR_UTC ? `${time} (on time)` : time;
};

export const getDailyCalendarHttpsUrl = (fileName: string): string => {
  if (typeof window === "undefined") return `/calendar/${fileName}`;
  return `https://vagudle.king-tajin.dev/calendar/${fileName}`;
};

export const getDailyCalendarWebcalUrl = (fileName: string): string =>
  getDailyCalendarHttpsUrl(fileName).replace(/^https?:/, "webcal:");

export const recordDailyStats = (date: string, won: boolean): DailyStats => {
  const stats = loadDailyStats();
  if (stats.lastCompletedDate === date) return stats;

  const updated: DailyStats = { ...stats, totalPlayed: stats.totalPlayed + 1 };

  if (won) {
    updated.totalWon = stats.totalWon + 1;
    updated.currentStreak =
      stats.lastCompletedDate && isDayAfter(stats.lastCompletedDate, date)
        ? stats.currentStreak + 1
        : 1;
    updated.bestStreak = Math.max(stats.bestStreak, updated.currentStreak);
  } else {
    updated.currentStreak = 0;
  }
  updated.lastCompletedDate = date;

  saveDailyStats(updated);
  return updated;
};
