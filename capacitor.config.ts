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
    SystemBars: {
      insetsHandling: "disable",
    },
    SafeArea: {
      statusBarStyle: "DARK",
      navigationBarStyle: "DARK",
    },
  },
};

export default config;
