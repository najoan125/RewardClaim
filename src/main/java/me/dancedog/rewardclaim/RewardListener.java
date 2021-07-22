package me.dancedog.rewardclaim;

import me.dancedog.rewardclaim.fetch.Request;
import me.dancedog.rewardclaim.fetch.Request.Method;
import me.dancedog.rewardclaim.fetch.Response;
import me.dancedog.rewardclaim.model.RewardSession;
import me.dancedog.rewardclaim.ui.GuiScreenRewardSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DanceDog / Ben on 3/29/20 @ 11:38 AM
 */
public class RewardListener {

    private static final Pattern REWARD_MESSAGE_PATTERN = Pattern.compile(
            "\u00A7r\u00A76Click the link to visit our website and claim your reward: \u00A7r\u00A7bhttps://rewards\\.hypixel\\.net/claim-reward/([A-Za-z0-9]+)\u00A7r");

    private long lastRewardOpenedMs = new Date().getTime();
    private final AtomicReference<RewardSession> sessionData = new AtomicReference<>();
    private boolean showGui = false;

    /**
     * Fetches & scrapes the reward page in a separate thread. The resulting json is then stored in
     * this class's rawRewardSessionData
     *
     * @param sessionId Session ID to scrape reward data from
     */
    private void fetchRewardSession(String sessionId) {
        // Make the claim request
        RewardClaim.pool.execute(() -> {
            final Future future = RewardClaim.pool.submit(() -> {
                try {
                    URL url = new URL("https://rewards.hypixel.net/claim-reward/" + sessionId);
                    Response response = new Request(url, Method.GET, null).execute();

                    if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                        Document document = Jsoup.parse(response.getBody());
                        RewardSession session = RewardScraper.parseSessionFromRewardPage(document, response.getNewCookies());
                        sessionData.set(session);
                    } else {
                        RewardClaim.printWarning("Server sent back a " + response.getStatusCode()
                                + " status code. Received the following body:\n" + response.getBody(), null, false);
                    }
                } catch (IOException e) {
                    RewardClaim.printWarning("IOException while fetching reward page", e, false);
                }
            });
            try {
                future.get();
                this.showGui = true;

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Checks to show the gui
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (showGui) {
            showGui = false;
            RewardSession currentSessionData = sessionData.getAndSet(null);
            if (currentSessionData != null) {
                if (currentSessionData.getError() != null) {
                    RewardClaim.printWarning("Failed to get reward: " + currentSessionData.getError(), null, true);
                    return;
                }
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenRewardSession(currentSessionData));
            }
        }
    }

    /**
     * Check for link to daily reward in chat
     */
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        Matcher chatMatcher = REWARD_MESSAGE_PATTERN.matcher(event.message.getFormattedText());
        if (chatMatcher.find()) {
            event.setCanceled(true);
            lastRewardOpenedMs = System.currentTimeMillis();

            String sessionId = chatMatcher.group(1);
            RewardClaim.getLogger().info("Triggered fetch for reward session #{}", sessionId);
            this.fetchRewardSession(sessionId);
        }
    }

    /**
     * Checks for the daily reward book (which Hypixel normally displays when clicking the reward
     * token) and cancels it completely
     */
    @SubscribeEvent
    public void onGuiInit(GuiOpenEvent event) {
        // Check for the reward book notification up to 10 seconds after the reward's chat link was received
        if (Minecraft.getMinecraft().thePlayer != null
                && event.gui instanceof GuiScreenBook
                && (System.currentTimeMillis() - lastRewardOpenedMs) <= 10000) {
            event.setCanceled(true);
            lastRewardOpenedMs = 0;
        }
    }
}
