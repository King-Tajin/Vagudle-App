import React from "react";
import { createRoot } from "react-dom/client";
import { LazyMotion, domAnimation, MotionConfig } from "framer-motion";
import { Capacitor } from "@capacitor/core";
import { App as CapacitorApp } from "@capacitor/app";
import "./index.css";
import App from "./App";
import { AlertProvider } from "./context/AlertContext";
import { initDiscordSDK } from "./lib/discord";
import { LinkDiscordPage } from "./components/screens/LinkDiscordPage";
import { LinkPlayGamesPage } from "./components/screens/LinkPlayGamesPage";
import { initCrashReporting } from "./lib/crashReporting";
import { CrashBoundary } from "./components/CrashBoundary";
import { listenForReminderNotificationTaps } from "./lib/notifications";
import { DAILY_PATH } from "./lib/daily";

const API_BASE = "https://vagudle.king-tajin.dev";
const _fetch = window.fetch.bind(window);
window.fetch = (input, init?) => {
  if (typeof input === "string" && input.startsWith("/api/")) {
    input = `${API_BASE}${input}`;
  }
  return _fetch(input, init);
};

const DEEP_LINK_HOST = "vagudle.king-tajin.dev";

function toLocalPath(url: string): string | null {
  try {
    const parsed = new URL(url);
    if (parsed.hostname !== DEEP_LINK_HOST) return null;
    return `${parsed.pathname}${parsed.search}${parsed.hash}`;
  } catch {
    return null;
  }
}

async function applyColdStartDeepLink(): Promise<boolean> {
  if (!Capacitor.isNativePlatform()) return false;

  const launch = await CapacitorApp.getLaunchUrl();
  if (!launch?.url) return false;

  const localPath = toLocalPath(launch.url);
  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`;
  if (!localPath || localPath === currentPath) return false;

  window.location.replace(localPath);
  return true;
}

function listenForWarmStartDeepLinks(): void {
  if (!Capacitor.isNativePlatform()) return;

  void CapacitorApp.addListener("appUrlOpen", ({ url }) => {
    const localPath = toLocalPath(url);
    const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`;
    if (localPath && localPath !== currentPath) {
      window.location.href = localPath;
    }
  });
}

function openDailyFromNotification(): void {
  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`;
  if (currentPath !== DAILY_PATH) {
    window.location.href = DAILY_PATH;
  }
}

async function bootstrap() {
  initCrashReporting();

  const redirecting = await applyColdStartDeepLink();
  if (redirecting) return;

  listenForWarmStartDeepLinks();
  listenForReminderNotificationTaps(openDailyFromNotification);

  const isLinkDiscordRoute = window.location.pathname === "/link-discord";
  const isLinkPlayGamesRoute = window.location.pathname === "/link-playgames";

  await initDiscordSDK();
  createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
      <LazyMotion features={domAnimation} strict>
        <MotionConfig reducedMotion="user">
          <CrashBoundary>
            <AlertProvider>
              {isLinkDiscordRoute ? (
                <LinkDiscordPage />
              ) : isLinkPlayGamesRoute ? (
                <LinkPlayGamesPage />
              ) : (
                <App />
              )}
            </AlertProvider>
          </CrashBoundary>
        </MotionConfig>
      </LazyMotion>
    </React.StrictMode>
  );
}

void bootstrap();
