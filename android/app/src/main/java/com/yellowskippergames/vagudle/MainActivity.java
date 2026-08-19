package com.yellowskippergames.vagudle;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.core.view.WindowCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

  private static final int LARGE_SCREEN_BREAKPOINT_DP = 600;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    registerPlugin(PlayGamesAuthPlugin.class);

    boolean isLargeScreen =
      getResources().getConfiguration().smallestScreenWidthDp >=
      LARGE_SCREEN_BREAKPOINT_DP;
    if (!isLargeScreen) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    super.onCreate(savedInstanceState);

    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
    WindowCompat.getInsetsController(
      getWindow(),
      getWindow().getDecorView()
    ).setAppearanceLightStatusBars(false);
    WindowCompat.getInsetsController(
      getWindow(),
      getWindow().getDecorView()
    ).setAppearanceLightNavigationBars(false);

    WebView webView = getBridge().getWebView();
    webView.setVerticalScrollBarEnabled(false);
    webView.setHorizontalScrollBarEnabled(false);
    webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

    WebSettings settings = webView.getSettings();
    settings.setSupportZoom(false);
    settings.setBuiltInZoomControls(false);
    settings.setDisplayZoomControls(false);
  }
}
