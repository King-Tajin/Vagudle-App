import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.yellowskippergames.vagudle",
  appName: "Vagudle",
  webDir: "dist",
  plugins: {
    FirebaseAuthentication: {
      skipNativeAuth: false,
      providers: ["google.com"],
    },
    LocalNotifications: {
      smallIcon: "ic_launcher_monochrome",
      iconColor: "#EAB308",
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
