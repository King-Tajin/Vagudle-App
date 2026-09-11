package com.yellowskippergames.vagudle;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "BackNavigation")
public class BackNavigationPlugin extends Plugin {

  public interface OnStateChangeListener {
    void onIsRootChanged(boolean isRoot);
  }

  private OnStateChangeListener listener;

  public void setOnStateChangeListener(OnStateChangeListener listener) {
    this.listener = listener;
  }

  @SuppressWarnings("unused")
  @PluginMethod
  public void setIsRoot(PluginCall call) {
    Boolean isRoot = call.getBoolean("isRoot", true);
    if (listener != null) {
      listener.onIsRootChanged(Boolean.TRUE.equals(isRoot));
    }
    call.resolve();
  }

  public void notifyBackStarted(float progress, int swipeEdge) {
    JSObject ret = new JSObject();
    ret.put("progress", progress);
    ret.put("swipeEdge", swipeEdge);
    notifyListeners("backStarted", ret);
  }

  public void notifyBackProgressed(float progress, int swipeEdge) {
    JSObject ret = new JSObject();
    ret.put("progress", progress);
    ret.put("swipeEdge", swipeEdge);
    notifyListeners("backProgressed", ret);
  }

  public void notifyBackCancelled() {
    notifyListeners("backCancelled", new JSObject());
  }
}
