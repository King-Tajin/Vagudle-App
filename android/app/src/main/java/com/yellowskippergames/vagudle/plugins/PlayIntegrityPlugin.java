package com.yellowskippergames.vagudle.plugins;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.StandardIntegrityManager;
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest;
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider;
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest;

@CapacitorPlugin(name = "PlayIntegrity")
public class PlayIntegrityPlugin extends Plugin {

  private static final long CLOUD_PROJECT_NUMBER = 76056105008L;

  private StandardIntegrityManager integrityManager;
  private Task<StandardIntegrityTokenProvider> preparation;

  @Override
  public void load() {
    getProvider(false);
  }

  @SuppressWarnings("unused")
  @PluginMethod
  public void requestToken(PluginCall call) {
    String requestHash = call.getString("requestHash");
    if (requestHash == null || requestHash.isEmpty()) {
      call.reject("requestHash is required.");
      return;
    }

    requestWithProvider(call, requestHash, false);
  }

  private void requestWithProvider(
    PluginCall call,
    String requestHash,
    boolean isRetry
  ) {
    getProvider(isRetry).addOnCompleteListener((providerTask) -> {
      if (!providerTask.isSuccessful()) {
        call.reject(
          "Play Integrity is unavailable: " + describe(providerTask),
          providerTask.getException()
        );
        return;
      }

      providerTask
        .getResult()
        .request(
          StandardIntegrityTokenRequest.builder()
            .setRequestHash(requestHash)
            .build()
        )
        .addOnCompleteListener((tokenTask) -> {
          if (tokenTask.isSuccessful()) {
            JSObject result = new JSObject();
            result.put("token", tokenTask.getResult().token());
            call.resolve(result);
            return;
          }

          if (!isRetry) {
            requestWithProvider(call, requestHash, true);
            return;
          }

          call.reject(
            "Play Integrity token request failed: " + describe(tokenTask),
            tokenTask.getException()
          );
        });
    });
  }

  private synchronized Task<StandardIntegrityTokenProvider> getProvider(
    boolean forceRefresh
  ) {
    boolean previousFailed =
      preparation != null &&
      preparation.isComplete() &&
      !preparation.isSuccessful();

    if (forceRefresh || previousFailed) {
      preparation = null;
    }

    if (preparation == null) {
      if (integrityManager == null) {
        integrityManager = IntegrityManagerFactory.createStandard(
          getContext().getApplicationContext()
        );
      }
      preparation = integrityManager.prepareIntegrityToken(
        PrepareIntegrityTokenRequest.builder()
          .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
          .build()
      );
    }

    return preparation;
  }

  private String describe(Task<?> task) {
    Exception error = task.getException();
    return error != null && error.getMessage() != null
      ? error.getMessage()
      : "unknown error";
  }
}
