package com.yellowskippergames.vagudle.plugins;

import android.view.View;
import android.webkit.JavascriptInterface;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.Plugin;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.Locale;

@CapacitorPlugin(name = "AppInsets")
public class AppInsetsPlugin extends Plugin {

  private static final String BRIDGE_NAME = "AppInsetsBridge";
  private static final String EMPTY_INSETS =
    "{\"top\":0,\"right\":0,\"bottom\":0,\"left\":0}";

  private volatile String currentInsetsJson = EMPTY_INSETS;

  @Override
  public void load() {
    super.load();
    getBridge()
      .getWebView()
      .addJavascriptInterface(new InsetsBridge(this), BRIDGE_NAME);
  }

  public void installInsetsListener() {
    View decorView = getActivity().getWindow().getDecorView();
    ViewCompat.setOnApplyWindowInsetsListener(
      decorView,
      this::onApplyWindowInsets
    );
    ViewCompat.requestApplyInsets(decorView);
  }

  private WindowInsetsCompat onApplyWindowInsets(
    View view,
    WindowInsetsCompat windowInsets
  ) {
    Insets bars = windowInsets.getInsets(
      WindowInsetsCompat.Type.systemBars() |
        WindowInsetsCompat.Type.displayCutout()
    );
    Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
    boolean keyboardVisible = windowInsets.isVisible(
      WindowInsetsCompat.Type.ime()
    );

    view.setPadding(0, 0, 0, keyboardVisible ? ime.bottom : 0);
    publish(bars.top, bars.right, keyboardVisible ? 0 : bars.bottom, bars.left);

    return windowInsets;
  }

  private void publish(int top, int right, int bottom, int left) {
    float density = getContext().getResources().getDisplayMetrics().density;
    String json = String.format(
      Locale.US,
      "{\"top\":%.2f,\"right\":%.2f,\"bottom\":%.2f,\"left\":%.2f}",
      top / density,
      right / density,
      bottom / density,
      left / density
    );
    if (json.equals(currentInsetsJson)) {
      return;
    }
    currentInsetsJson = json;
    getBridge()
      .getWebView()
      .evaluateJavascript(
        "window.__applyAppInsets&&window.__applyAppInsets(" + json + ")",
        null
      );
  }

  private record InsetsBridge(AppInsetsPlugin plugin) {
    @SuppressWarnings("unused")
    @JavascriptInterface
    public String get() {
      return plugin.currentInsetsJson;
    }
  }
}
