package com.yellowskippergames.vagudle;

import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import java.util.concurrent.TimeUnit;

@CapacitorPlugin(name = "ReviewPrompt")
public class ReviewPromptPlugin extends Plugin {

  private static final String PREFS_NAME = "vagudle_review_prefs";
  private static final String KEY_LAST_REQUEST_MILLIS =
    "last_review_request_millis";
  private static final String KEY_REQUEST_COUNT = "review_request_count";
  private static final long MIN_MILLIS_BETWEEN_REQUESTS =
    TimeUnit.DAYS.toMillis(30);
  private static final int MAX_LIFETIME_REQUESTS = 3;

  @SuppressWarnings("unused")
  @PluginMethod
  public void requestReview(PluginCall call) {
    SharedPreferences prefs = getContext().getSharedPreferences(
      PREFS_NAME,
      Context.MODE_PRIVATE
    );
    int requestCount = prefs.getInt(KEY_REQUEST_COUNT, 0);
    long lastRequestMillis = prefs.getLong(KEY_LAST_REQUEST_MILLIS, 0);
    long now = System.currentTimeMillis();

    if (
      requestCount >= MAX_LIFETIME_REQUESTS ||
      now - lastRequestMillis < MIN_MILLIS_BETWEEN_REQUESTS
    ) {
      resolveRequested(call, false);
      return;
    }

    prefs
      .edit()
      .putInt(KEY_REQUEST_COUNT, requestCount + 1)
      .putLong(KEY_LAST_REQUEST_MILLIS, now)
      .apply();

    ReviewManager reviewManager = ReviewManagerFactory.create(getContext());
    reviewManager.requestReviewFlow().addOnCompleteListener((requestTask) -> {
      if (!requestTask.isSuccessful()) {
        resolveRequested(call, false);
        return;
      }

      ReviewInfo reviewInfo = requestTask.getResult();
      reviewManager
        .launchReviewFlow(getActivity(), reviewInfo)
        .addOnCompleteListener((flowTask) -> resolveRequested(call, true));
    });
  }

  private void resolveRequested(PluginCall call, boolean requested) {
    JSObject result = new JSObject();
    result.put("requested", requested);
    call.resolve(result);
  }
}
