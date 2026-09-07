package com.yellowskippergames.vagudle;

import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "DailyWidget")
public class DailyWidgetPlugin extends Plugin {

  @SuppressWarnings("unused")
  @PluginMethod
  public void syncWidgetData(PluginCall call) {
    JSObject payload = call.getData();
    DailyWidgetData data;
    try {
      data = DailyWidgetDataKt.parseDailyWidgetPayload(payload);
    } catch (Exception e) {
      call.reject("Invalid widget payload.", e);
      return;
    }

    SharedPreferences prefs = getContext().getSharedPreferences(
      DailyWidgetDataKt.DAILY_WIDGET_PREFS_NAME,
      Context.MODE_PRIVATE
    );
    boolean saved = DailyWidgetDataKt.saveDailyWidgetData(prefs, data, true);
    if (!saved) {
      call.reject("Failed to persist widget data.");
      return;
    }

    try {
      DailyWidgetKt.requestDailyWidgetUpdate(getContext());
      DailyRefreshSchedulerKt.scheduleDailyRefresh(getContext());
    } catch (Exception ignored) {}

    call.resolve();
  }
}
