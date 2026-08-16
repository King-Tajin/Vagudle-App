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

  CapacitorApp.addListener("appUrlOpen", ({ url }) => {
    const localPath = toLocalPath(url);
    if (localPath) {
      window.location.href = localPath;
    }
  });
}

async function bootstrap() {
  const redirecting = await applyColdStartDeepLink();
  if (redirecting) return;

  listenForWarmStartDeepLinks();

  const isLinkDiscordRoute = window.location.pathname === "/link-discord";

  await initDiscordSDK();
  createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
      <LazyMotion features={domAnimation} strict>
        <MotionConfig reducedMotion="user">
          <AlertProvider>
            {isLinkDiscordRoute ? <LinkDiscordPage /> : <App />}
          </AlertProvider>
        </MotionConfig>
      </LazyMotion>
    </React.StrictMode>
  );
}

void bootstrap();
