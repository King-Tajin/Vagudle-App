package com.yellowskippergames.vagudle;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;

@CapacitorPlugin(name = "PlayGamesAuth")
public class PlayGamesAuthPlugin extends Plugin {

  private static final String WEB_CLIENT_ID =
    "76056105008-5j81emvd1qk891mb3kmlusisjeslorau.apps.googleusercontent.com";

  @PluginMethod
  public void signIn(PluginCall call) {
    GamesSignInClient signInClient = PlayGames.getGamesSignInClient(
      getActivity()
    );

    signInClient.isAuthenticated().addOnCompleteListener((isAuthTask) -> {
      boolean isAuthenticated =
        isAuthTask.isSuccessful() && isAuthTask.getResult().isAuthenticated();

      if (isAuthenticated) {
        requestServerAuthCode(call, signInClient);
        return;
      }

      signInClient.signIn().addOnCompleteListener((signInTask) -> {
        boolean signedIn =
          signInTask.isSuccessful() && signInTask.getResult().isAuthenticated();
        if (signedIn) {
          requestServerAuthCode(call, signInClient);
        } else {
          call.reject("Play Games sign-in failed.");
        }
      });
    });
  }

  private void requestServerAuthCode(
    PluginCall call,
    GamesSignInClient signInClient
  ) {
    signInClient
      .requestServerSideAccess(WEB_CLIENT_ID, false)
      .addOnCompleteListener((codeTask) -> {
        if (codeTask.isSuccessful()) {
          JSObject result = new JSObject();
          result.put("serverAuthCode", codeTask.getResult());
          call.resolve(result);
        } else {
          call.reject("Failed to get Play Games server auth code.");
        }
      });
  }

  @PluginMethod
  public void unlockAchievement(PluginCall call) {
    String achievementId = call.getString("achievementId");
    if (achievementId == null) {
      call.reject("achievementId is required.");
      return;
    }

    GamesSignInClient signInClient = PlayGames.getGamesSignInClient(
      getActivity()
    );

    signInClient.isAuthenticated().addOnCompleteListener((isAuthTask) -> {
      boolean isAuthenticated =
        isAuthTask.isSuccessful() && isAuthTask.getResult().isAuthenticated();

      if (!isAuthenticated) {
        call.reject("Not signed in to Play Games.");
        return;
      }

      AchievementsClient achievementsClient = PlayGames.getAchievementsClient(
        getActivity()
      );
      achievementsClient.unlock(achievementId);
      call.resolve();
    });
  }

  @PluginMethod
  public void setAchievementSteps(PluginCall call) {
    String achievementId = call.getString("achievementId");
    Integer steps = call.getInt("steps");
    if (achievementId == null || steps == null) {
      call.reject("achievementId and steps are required.");
      return;
    }

    GamesSignInClient signInClient = PlayGames.getGamesSignInClient(
      getActivity()
    );

    signInClient.isAuthenticated().addOnCompleteListener((isAuthTask) -> {
      boolean isAuthenticated =
        isAuthTask.isSuccessful() && isAuthTask.getResult().isAuthenticated();

      if (!isAuthenticated) {
        call.reject("Not signed in to Play Games.");
        return;
      }

      AchievementsClient achievementsClient = PlayGames.getAchievementsClient(
        getActivity()
      );
      achievementsClient.setSteps(achievementId, steps);
      call.resolve();
    });
  }
}
