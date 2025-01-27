package me.dancedog.rewardclaim;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@net.minecraftforge.fml.common.Mod(
        modid = RewardClaim.MODID,
        version = RewardClaim.VERSION,
        name = RewardClaim.MODNAME,
        useMetadata = true)
public class RewardClaim {

    public static final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    public static final String MODID = "rewardclaim";
    public static final String VERSION = "0.2";
    static final String MODNAME = "RewardClaim";

    private static Logger logger = LogManager.getLogger(MODID);

    public static Logger getLogger() {
        return RewardClaim.logger;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new RewardListener());
    }

    /**
     * Get a resource from the mod's GUI asset folder
     *
     * @param path Path to the resource (appended to /gui/)
     * @return ResourceLocation of the requested asset
     */
    public static ResourceLocation getGuiTexture(String path) {
        return new ResourceLocation(MODID + ":textures/gui/" + path);
    }

    public static void printWarning(String message, Throwable t, boolean inChat) {
        logger.warn(message, t);

        if (inChat && Minecraft.getMinecraft().thePlayer != null) {
            ChatComponentText chatMessage = new ChatComponentText("[" + MODNAME + "] " + message);
            chatMessage.getChatStyle().setBold(true).setColor(EnumChatFormatting.RED);
            Minecraft.getMinecraft().thePlayer.addChatMessage(chatMessage);
        }
    }
}
