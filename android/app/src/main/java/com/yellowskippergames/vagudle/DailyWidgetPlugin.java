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
    DailyWidgetData existing = DailyWidgetDataKt.loadDailyWidgetData(
      getContext()
    );
    DailyWidgetRank previousRank = existing != null ? existing.getRank() : null;
    DailyWidgetData data;
    try {
      data = DailyWidgetDataKt.parseDailyWidgetPayload(payload, previousRank);
    } catch (Exception e) {
      call.reject("Invalid widget payload.", e);
      return;
    }

    SharedPreferences prefs = getContext().getSharedPreferences(
      DailyWidgetDataKt.DAILY_WIDGET_PREFS_NAME,
      Context.MODE_PRIVATE
    );
    DailyWidgetDataKt.saveDailyWidgetData(prefs, data);

    try {
      DailyWidgetUpdateWorkerKt.requestDailyWidgetUpdate(getContext());
      DailyRefreshSchedulerKt.scheduleDailyRefresh(getContext());
    } catch (Exception ignored) {}

    call.resolve();
  }
}
