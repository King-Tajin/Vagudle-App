import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.yellowskippergames.vagudle",
  appName: "Vagudle",
  webDir: "dist",
  plugins: {
    LocalNotifications: {
      smallIcon: "ic_launcher_monochrome",
      iconColor: "#22C55E",
    },
  },
};

export default config;
