import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import svgr from "vite-plugin-svgr";
import path from "path";

export default defineConfig({
  plugins: [react(), svgr()],
  define: {
    "process.env": "import.meta.env",
  },
  resolve: {
    alias: {
      "@": path.resolve(import.meta.dirname, "./src"),
    },
  },
  build: {
    rolldownOptions: {
      output: {
        entryFileNames: "assets/[name].js",
        chunkFileNames: "assets/[name].js",
        assetFileNames: "assets/[name].[ext]",
        codeSplitting: {
          groups: [
            { name: "vendor-react", test: /node_modules\/(react|react-dom)\// },
            { name: "vendor-framer", test: /node_modules\/framer-motion\// },
            { name: "vendor-lottie", test: /node_modules\/lottie-web\// },
            {
              name: "vendor-twemoji",
              test: /node_modules\/(twemoji|unicode-emoji-json)\//,
            },
            {
              name: "vendor-fluid",
              test: /node_modules\/webgl-fluid-enhanced\//,
            },
            {
              name: "vendor-firebase",
              test: /node_modules\/(@firebase|firebase)\//,
            },
          ],
        },
      },
    },
  },
});
