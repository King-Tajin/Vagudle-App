package com.yellowskippergames.vagudle;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;
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
      Dialog dialog = new Dialog(getActivity(), R.style.PixelDialogTheme);
      dialog.setContentView(R.layout.dialog_prompt);
      dialog.setCancelable(true);

      TextView titleView = dialog.findViewById(R.id.dialog_title);
      TextView messageView = dialog.findViewById(R.id.dialog_message);
      Button secondaryButton = dialog.findViewById(R.id.btn_secondary);
      Button primaryButton = dialog.findViewById(R.id.btn_primary);

      titleView.setText(R.string.notification_primer_title);
      messageView.setText(R.string.notification_primer_message);
      secondaryButton.setText(R.string.notification_primer_not_now);
      primaryButton.setText(R.string.notification_primer_enable);

      secondaryButton.setOnClickListener((view) -> {
        dialog.dismiss();
        resolvePrimer(call, false);
      });
      primaryButton.setOnClickListener((view) -> {
        dialog.dismiss();
        resolvePrimer(call, true);
      });
      dialog.setOnCancelListener((dialogInterface) ->
        resolvePrimer(call, false)
      );

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
