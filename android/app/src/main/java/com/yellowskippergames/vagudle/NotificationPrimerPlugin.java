package com.yellowskippergames.vagudle;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NotificationPrimer")
public class NotificationPrimerPlugin extends Plugin {

  private static final String PREFS_NAME = "vagudle_notification_prefs";
  private static final String KEY_PRIMER_SHOWN = "notification_primer_shown";

  @SuppressWarnings("unused")
  @PluginMethod
  public void showPrimer(PluginCall call) {
    SharedPreferences prefs = getContext().getSharedPreferences(
      PREFS_NAME,
      Context.MODE_PRIVATE
    );

    if (prefs.getBoolean(KEY_PRIMER_SHOWN, false)) {
      JSObject result = new JSObject();
      result.put("alreadyShown", true);
      result.put("accepted", false);
      call.resolve(result);
      return;
    }

    prefs.edit().putBoolean(KEY_PRIMER_SHOWN, true).apply();

    getActivity().runOnUiThread(() -> {
      AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setTitle("Stay on your streak")
        .setMessage(
          "Get occasional reminders to play your daily puzzle and keep your streak alive. You can change this anytime in Settings."
        )
        .setPositiveButton("Enable Reminders", (dialogInterface, which) ->
          resolvePrimer(call, true)
        )
        .setNegativeButton("Not Now", (dialogInterface, which) ->
          resolvePrimer(call, false)
        )
        .setOnCancelListener((dialogInterface) -> resolvePrimer(call, false))
        .create();

      dialog.show();
    });
  }

  private void resolvePrimer(PluginCall call, boolean accepted) {
    JSObject result = new JSObject();
    result.put("alreadyShown", false);
    result.put("accepted", accepted);
    call.resolve(result);
  }
}
