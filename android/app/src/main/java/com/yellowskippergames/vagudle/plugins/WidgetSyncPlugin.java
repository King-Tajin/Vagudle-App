package com.yellowskippergames.vagudle.plugins;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.yellowskippergames.vagudle.widget.sync.WidgetSyncKt;

@CapacitorPlugin(name = "WidgetSync")
public class WidgetSyncPlugin extends Plugin {

  @SuppressWarnings("unused")
  @PluginMethod
  public void syncWidgetData(PluginCall call) {
    try {
      WidgetSyncKt.syncWidget(
        getContext(),
        call.getString("widget"),
        call.getObject("payload")
      );
    } catch (Exception e) {
      call.reject("Invalid widget payload.", e);
      return;
    }
    call.resolve();
  }
}
