package com.yellowskippergames.vagudle;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import com.getcapacitor.BridgeActivity;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;

public class MainActivity extends BridgeActivity {

  private static final int LARGE_SCREEN_BREAKPOINT_DP = 600;
  private static final String UPDATE_PREFS_NAME = "update_prompt";
  private static final String DISMISSED_VERSION_KEY = "dismissed_version_code";

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    registerPlugin(PlayGamesAuthPlugin.class);
    registerPlugin(NotificationPrimerPlugin.class);
    registerPlugin(ReviewPromptPlugin.class);

    boolean isLargeScreen =
      getResources().getConfiguration().smallestScreenWidthDp >=
      LARGE_SCREEN_BREAKPOINT_DP;
    if (!isLargeScreen) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

    WebView webView = getBridge().getWebView();
    webView.setVerticalScrollBarEnabled(false);
    webView.setHorizontalScrollBarEnabled(false);
    webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

    WebSettings settings = webView.getSettings();
    settings.setSupportZoom(false);
    settings.setBuiltInZoomControls(false);
    settings.setDisplayZoomControls(false);

    checkForUpdate();
  }

  private void checkForUpdate() {
    AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
    appUpdateManager
      .getAppUpdateInfo()
      .addOnSuccessListener(this::handleAppUpdateInfo)
      .addOnFailureListener((error) -> {});
  }

  private void handleAppUpdateInfo(AppUpdateInfo appUpdateInfo) {
    if (
      appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE
    ) {
      return;
    }

    int availableVersionCode = appUpdateInfo.availableVersionCode();
    SharedPreferences prefs = getSharedPreferences(
      UPDATE_PREFS_NAME,
      MODE_PRIVATE
    );
    int dismissedVersionCode = prefs.getInt(DISMISSED_VERSION_KEY, 0);
    if (availableVersionCode <= dismissedVersionCode) {
      return;
    }

    if (isFinishing() || isDestroyed()) {
      return;
    }

    Dialog dialog = new Dialog(this, R.style.PixelDialogTheme);
    dialog.setContentView(R.layout.dialog_prompt);
    dialog.setCancelable(true);

    TextView titleView = dialog.findViewById(R.id.dialog_title);
    TextView messageView = dialog.findViewById(R.id.dialog_message);
    Button secondaryButton = dialog.findViewById(R.id.btn_secondary);
    Button primaryButton = dialog.findViewById(R.id.btn_primary);

    titleView.setText(R.string.update_available_title);
    messageView.setText(R.string.update_available_message);
    secondaryButton.setText(R.string.update_available_not_now);
    primaryButton.setText(R.string.update_available_update);

    secondaryButton.setOnClickListener((view) -> {
      dialog.dismiss();
      rememberDismissedVersion(availableVersionCode);
    });
    primaryButton.setOnClickListener((view) -> {
      dialog.dismiss();
      rememberDismissedVersion(availableVersionCode);
      openPlayStoreListing();
    });

    dialog.show();
  }

  private void rememberDismissedVersion(int versionCode) {
    getSharedPreferences(UPDATE_PREFS_NAME, MODE_PRIVATE)
      .edit()
      .putInt(DISMISSED_VERSION_KEY, versionCode)
      .apply();
  }

  private void openPlayStoreListing() {
    String packageName = getPackageName();
    try {
      startActivity(
        new Intent(
          Intent.ACTION_VIEW,
          Uri.parse("market://details?id=" + packageName)
        )
      );
    } catch (ActivityNotFoundException e) {
      startActivity(
        new Intent(
          Intent.ACTION_VIEW,
          Uri.parse(
            "https://play.google.com/store/apps/details?id=" + packageName
          )
        )
      );
    }
  }
}
