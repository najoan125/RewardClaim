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
            "^\\n\u00a7r\u00a76(?:Click the link to visit our website and claim your reward: |Click th' link t' visit our website an' plunder yer reward: |Clica no link para visitares o nosso site e receberes o prémio: |Klicke den Link, um unsere Webseite zu besuchen und deine Belohnung abzuholen: |Cliquez sur le lien pour visiter notre site et r\u00e9clamer votre r\u00e9compense: |Klik de link om onze website te bezoeken, en je beloning te verkrijgen: |Haz clic en el link para visitar nuestra web y recoger tu recompensa: |Clique no link para visitar o nosso site e reivindicar sua recompensa: |Clicca il link per visitare il sito e riscattare la tua ricompensa: |Kliknij link, aby odwiedzi\u0107 nasz\u0105 stron\u0119 internetow\u0105 i odebra\u0107 swoj\u0105 nagrod\u0119: |\u70b9\u51fb\u94fe\u63a5\u8bbf\u95ee\u6211\u4eec\u7684\u7f51\u7ad9\u5e76\u9886\u53d6\u5956\u52b1\uFF1A|\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u043d\u0430 \u0441\u0441\u044b\u043b\u043a\u0443, \u0447\u0442\u043e\u0431\u044b \u043f\u0435\u0440\u0435\u0439\u0442\u0438 \u043d\u0430 \u043d\u0430\u0448 \u0441\u0430\u0439\u0442 \u0438 \u0437\u0430\u0431\u0440\u0430\u0442\u044c \u0441\u0432\u043e\u044e \u043d\u0430\u0433\u0440\u0430\u0434\u0443: |\uc800\ud76c\uc758 \uc6f9 \uc0ac\uc774\ud2b8\uc5d0 \ubc29\ubb38\ud558\uace0 \ubcf4\uc0c1\uc744 \uc218\ub839\ud558\ub824\uba74 \ub9c1\ud06c\ub97c \ud074\ub9ad\ud558\uc138\uc694: |\u30EA\u30F3\u30AF\u3092\u30AF\u30EA\u30C3\u30AF\u3057\u3066\u30A6\u30A7\u30D6\u30B5\u30A4\u30C8\u306B\u30A2\u30AF\u30BB\u30B9\u3057\u3001\u5831\u916C\u3092\u7372\u5F97\u3057\u3066\u304F\u3060\u3055\u3044\uFF1A)\u00a7r\u00a7b(?:https?://rewards\\.hypixel\\.net/claim-reward/([a-zA-Z0-9]{0,12}))\u00a7r\\n\u00a7r$");

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
