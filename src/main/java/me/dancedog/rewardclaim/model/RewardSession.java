package me.dancedog.rewardclaim.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.dancedog.rewardclaim.RewardClaim;
import me.dancedog.rewardclaim.fetch.Request;
import me.dancedog.rewardclaim.fetch.Request.Method;
import me.dancedog.rewardclaim.fetch.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DanceDog / Ben on 3/22/20 @ 8:52 PM
 */
public class RewardSession {

    String error;
    private String id;
    private List<RewardCard> cards;
    private String csrfToken;
    private String cookie;
    private String activeAd;
    private String currentStreak;
    private String topStreak;

    /**
     * Create a new reward session object from the session json (rewards, ad, streak, etc), the
     * session's csrf token and the session's cookie
     *
     * @param raw    The session's raw json representation
     * @param cookie The cookie received from the original reward request
     */
    public RewardSession(JsonObject raw, String cookie) {
        if (!validateSessionData(raw)) {
            if (raw != null && raw.has("error")) {
                this.error = raw.get("error").getAsString();
            } else {
                this.error = "Invalid reward session data";
            }
            return;
        }

        this.id = raw.get("id").getAsString();
        this.activeAd = raw.get("activeAd").getAsString();
        this.cards = new ArrayList<>();
        this.currentStreak = raw.get("dailyStreak").getAsJsonObject().get("score").getAsString();
        this.currentStreak = raw.get("dailyStreak").getAsJsonObject().get("highScore").getAsString();
        for (JsonElement rewardElement : raw.get("rewards").getAsJsonArray()) {
            this.cards.add(new RewardCard(rewardElement != null ? rewardElement.getAsJsonObject() : null));
        }
        this.csrfToken = raw.get("_csrf").getAsString();
        this.cookie = cookie;
    }

    private static boolean validateSessionData(JsonObject raw) {
        return raw != null
                && raw.has("id")
                && raw.has("activeAd")
                && raw.has("ad")
                && raw.has("skippable")
                && raw.has("rewards")
                && raw.has("dailyStreak")
                && raw.get("rewards").getAsJsonArray().size() == 3;
    }

    public void claimReward(int option) {
        new Thread(() -> {
            try {
                String urlStr = "https://rewards.hypixel.net/claim-reward/claim"
                        + "?option=" + option
                        + "&id=" + this.id
                        + "&_csrf=" + this.csrfToken
                        + "&activeAd=" + (this.activeAd != null ? this.activeAd : "0")
                        + "&watchedFallback=false";
                URL url = new URL(urlStr);
                Response response = new Request(url, Method.POST, this.cookie).execute();
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    RewardClaim.getLogger().info("Successfully claimed reward");
                } else {
                    RewardClaim.printWarning("Failed to claim reward. Server sent back a " + response.getStatusCode()
                            + " status code. Received the following body:\n" + response.getBody(), null, false);
                }
            } catch (IOException e) {
                RewardClaim.printWarning("IOException during claim reward request", e, false);
            }
        }).start();
    }

    public String getActiveAd() {
        return this.activeAd;
    }

    public String getError() {
        return this.error;
    }

    public String getId() {
        return this.id;
    }

    public List<RewardCard> getCards() {
        return this.cards;
    }

    public String getCsrfToken() {
        return this.csrfToken;
    }

    public String getCookie() {
        return this.cookie;
    }

    public String getCurrentStreak() {
        return this.currentStreak;
    }

    public String getTopStreak() {
        return topStreak;
    }
}
