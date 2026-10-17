package com.yellowskippergames.vagudle.plugins;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.play.core.assetpacks.AssetPackLocation;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import java.util.Collections;

@CapacitorPlugin(name = "AssetPacks")
public class AssetPacksPlugin extends Plugin {

  private static final int STATUS_PENDING = 1;
  private static final int STATUS_DOWNLOADING = 2;
  private static final int STATUS_TRANSFERRING = 3;
  private static final int STATUS_COMPLETED = 4;
  private static final int STATUS_FAILED = 5;
  private static final int STATUS_CANCELED = 6;
  private static final int STATUS_WAITING_FOR_WIFI = 7;
  private static final int STATUS_NOT_INSTALLED = 8;
  private static final int STATUS_REQUIRES_USER_CONFIRMATION = 9;

  private AssetPackManager assetPackManager;
  private AssetPackStateUpdateListener stateListener;

  @Override
  public void load() {
    assetPackManager = AssetPackManagerFactory.getInstance(getContext());
    stateListener = this::emitState;
    assetPackManager.registerListener(stateListener);
  }

  @Override
  protected void handleOnDestroy() {
    if (assetPackManager != null && stateListener != null) {
      assetPackManager.unregisterListener(stateListener);
    }
    super.handleOnDestroy();
  }

  @SuppressWarnings("unused")
  @PluginMethod
  public void getLocation(PluginCall call) {
    String name = call.getString("name");
    if (name == null || name.isEmpty()) {
      call.reject("Missing pack name");
      return;
    }

    JSObject result = new JSObject();
    result.put("path", findAssetsPath(name));
    call.resolve(result);
  }

  @SuppressWarnings("unused")
  @PluginMethod
  public void fetch(PluginCall call) {
    String name = call.getString("name");
    if (name == null || name.isEmpty()) {
      call.reject("Missing pack name");
      return;
    }

    if (findAssetsPath(name) != null) {
      call.resolve(completedState(name));
      return;
    }

    assetPackManager
      .fetch(Collections.singletonList(name))
      .addOnSuccessListener((states) -> {
        AssetPackState state = states.packStates().get(name);
        if (state == null) {
          call.reject("Unknown asset pack: " + name);
          return;
        }
        call.resolve(toJs(state));
      })
      .addOnFailureListener((error) ->
        call.reject(
          error.getMessage() != null ? error.getMessage() : "Fetch failed"
        )
      );
  }

  private String findAssetsPath(String name) {
    AssetPackLocation location = assetPackManager.getPackLocation(name);
    return location != null ? location.assetsPath() : null;
  }

  private void emitState(AssetPackState state) {
    notifyListeners("packState", toJs(state));
  }

  private JSObject completedState(String name) {
    JSObject result = new JSObject();
    result.put("name", name);
    result.put("status", statusName(STATUS_COMPLETED));
    result.put("bytesDownloaded", 0);
    result.put("totalBytes", 0);
    result.put("percent", 100);
    result.put("errorCode", 0);
    return result;
  }

  private JSObject toJs(AssetPackState state) {
    JSObject result = new JSObject();
    result.put("name", state.name());
    result.put("status", statusName(state.status()));
    result.put("bytesDownloaded", state.bytesDownloaded());
    result.put("totalBytes", state.totalBytesToDownload());
    result.put("percent", state.transferProgressPercentage());
    result.put("errorCode", state.errorCode());
    return result;
  }

  private String statusName(int status) {
    return switch (status) {
      case STATUS_PENDING -> "pending";
      case STATUS_DOWNLOADING -> "downloading";
      case STATUS_TRANSFERRING -> "transferring";
      case STATUS_COMPLETED -> "completed";
      case STATUS_FAILED -> "failed";
      case STATUS_CANCELED -> "canceled";
      case STATUS_WAITING_FOR_WIFI -> "waiting_for_wifi";
      case STATUS_NOT_INSTALLED -> "not_installed";
      case STATUS_REQUIRES_USER_CONFIRMATION -> "requires_user_confirmation";
      default -> "unknown";
    };
  }
}
