import React from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App";
import { AlertProvider } from "./context/AlertContext";
import { initDiscordSDK } from "./lib/discord";

const API_BASE = "https://vagudle.king-tajin.dev";
const _fetch = window.fetch.bind(window);
window.fetch = (input, init?) => {
  if (typeof input === "string" && input.startsWith("/api/")) {
    input = `${API_BASE}${input}`;
  }
  return _fetch(input, init);
};

async function bootstrap() {
  await initDiscordSDK();
  createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
      <AlertProvider>
        <App />
      </AlertProvider>
    </React.StrictMode>
  );
}

void bootstrap();
