import { getGuessStatuses } from "./statuses";
import { unicodeSplit } from "./words";
import { GAME_TITLE } from "../constants/strings";
import { type GameStats } from "./localStorage";
import { DAILY_PATH } from "./daily";
import { UAParser } from "ua-parser-js";
import { DICT_LABELS, type ChallengeConfig } from "./challenge";
import type { Achievement } from "./achievements";
import { BACKGROUNDS } from "./backgrounds";

export type CapacitorShareOptions = {
  title?: string;
  text?: string;
  url?: string;
  dialogTitle?: string;
  files?: string[];
};

export type CapacitorSharePlugin = {
  share: (options: CapacitorShareOptions) => Promise<{ activityType?: string }>;
};

const parser = new UAParser();
const browser = parser.getBrowser();

const EMOJI_TILES = ["🟩", "🟨", "⬛"];

const attemptShare = (shareData: object) => {
  return (
    browser.name?.toUpperCase().indexOf("FIREFOX") === -1 &&
    typeof navigator.share === "function" &&
    navigator.canShare &&
    navigator.canShare(shareData)
  );
};

const getSharePlugin = (): CapacitorSharePlugin | null => {
  if (typeof window === "undefined") return null;
  if (!window.Capacitor?.isNativePlatform?.()) return null;
  return window.Capacitor.Plugins?.Share ?? null;
};

export const doShare = async (
  shareData: { title: string; text: string },
  textToShare: string,
  handleShareToClipboard: () => void
) => {
  try {
    const sharePlugin = getSharePlugin();
    if (sharePlugin) {
      await sharePlugin.share(shareData);
      return;
    }
    if (attemptShare(shareData)) {
      await navigator.share(shareData);
      return;
    }
  } catch {}
  try {
    await navigator.clipboard.writeText(textToShare);
    handleShareToClipboard();
  } catch {}
};

export const shareStatus = async (
  solution: string,
  guesses: string[],
  lost: boolean,
  handleShareToClipboard: () => void,
  hardMode: boolean,
  maxChallenges: number,
  challengeMode: boolean = false
) => {
  const score = lost ? "X" : guesses.length;
  const modeTag = hardMode ? " [HARD]" : "";
  const wordPart = challengeMode ? `${solution.length} letters` : solution;
  const header = challengeMode
    ? `${GAME_TITLE} [CHALLENGE] — ${score}/${maxChallenges} (${wordPart})`
    : `${GAME_TITLE}${modeTag} — ${solution} — ${score}/${maxChallenges} (${solution.length} letters)`;
  const textToShare =
    `${header}\n${window.location.href}\n` +
    generateEmojiGrid(solution, guesses, EMOJI_TILES);

  await doShare(
    {
      title: challengeMode
        ? `${GAME_TITLE} Challenge`
        : `${GAME_TITLE} — ${solution}`,
      text: textToShare,
    },
    textToShare,
    handleShareToClipboard
  );
};

export const shareDailyResult = async (
  solution: string,
  guesses: string[],
  lost: boolean,
  dailyNumber: number,
  maxChallenges: number,
  handleShareToClipboard: () => void
) => {
  const score = lost ? "X" : guesses.length;
  const header = `${GAME_TITLE} Daily #${dailyNumber} — ${score}/${maxChallenges}`;
  const textToShare =
    `${header}\nhttps://vagudle.king-tajin.dev${DAILY_PATH}\n` +
    generateEmojiGrid(solution, guesses, EMOJI_TILES);

  await doShare(
    { title: `${GAME_TITLE} Daily #${dailyNumber}`, text: textToShare },
    textToShare,
    handleShareToClipboard
  );
};

export const generateEmojiGrid = (
  solution: string,
  guesses: string[],
  tiles: string[]
) => {
  return guesses
    .map((guess) => {
      const status = getGuessStatuses(solution, guess);
      const splitGuess = unicodeSplit(guess);
      return splitGuess
        .map((_, i) => {
          switch (status[i]) {
            case "correct":
              return tiles[0];
            case "present":
              return tiles[1];
            default:
              return tiles[2];
          }
        })
        .join("");
    })
    .join("\n");
};

export const shareStats = async (
  stats: GameStats,
  hardMode: boolean,
  handleShareToClipboard: () => void
) => {
  const modeTag = hardMode ? " [HARD]" : " [NORMAL]";
  const lines = [
    `${GAME_TITLE}${modeTag} Stats`,
    `${window.location.href}`,
    ``,
    `🎮 Played:   ${stats.totalGames}`,
    `✅ Win Rate: ${stats.successRate}%`,
    `🔥 Streak:   ${stats.currentStreak}`,
    `🏆 Best:     ${stats.bestStreak}`,
  ];

  const maxCount = Math.max(...stats.winDistribution, 1);
  const bars = stats.winDistribution
    .map((count, i) => {
      const filled = Math.round((count / maxCount) * 8);
      const bar = "█".repeat(filled) + "░".repeat(8 - filled);
      return `${String(i + 1).padStart(2)}: ${bar} ${count}`;
    })
    .join("\n");

  lines.push(``, `Guess Distribution:`, bars);

  const textToShare = lines.join("\n");

  await doShare(
    { title: `${GAME_TITLE}${modeTag} Stats`, text: textToShare },
    textToShare,
    handleShareToClipboard
  );
};

export const shareChallengeInvite = async (
  config: ChallengeConfig,
  handleShareToClipboard: () => void
) => {
  const text =
    `I'm challenging you to a custom Vagudle!\n` +
    `${config.length} letters · ${DICT_LABELS[config.dict]} dictionary · ${
      config.guesses
    } guesses\n` +
    `(Results won't affect your stats)\n` +
    window.location.href;

  await doShare(
    { title: "Vagudle Challenge", text },
    text,
    handleShareToClipboard
  );
};

export const shareAchievement = async (
  achievement: Achievement,
  handleShareToClipboard: () => void
) => {
  const bgUnlock = BACKGROUNDS.find(
    (b) => b.requiresAchievementId === achievement.id
  );
  const text =
    `🏆 Achievement Unlocked: ${achievement.title}\n` +
    `${achievement.description}\n` +
    (bgUnlock ? `Unlocked background: ${bgUnlock.desktopLabel}\n` : "") +
    window.location.href;

  await doShare(
    { title: "Vagudle Achievement", text },
    text,
    handleShareToClipboard
  );
};
