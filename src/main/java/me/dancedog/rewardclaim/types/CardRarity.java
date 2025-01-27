package me.dancedog.rewardclaim.types;

import me.dancedog.rewardclaim.RewardClaim;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

/**
 * A card's rarity value Determines the card's appearance
 * <p>
 * Created by DanceDog / Ben on 3/22/20 @ 9:22 PM
 */
@SuppressWarnings("unused")
public enum CardRarity {
    COMMON("rarity.common", 0xff9b4e, EnumChatFormatting.YELLOW),
    RARE("rarity.rare", 0x62e2de, EnumChatFormatting.AQUA),
    EPIC("rarity.epic", 0xc633e8, EnumChatFormatting.DARK_PURPLE),
    LEGENDARY("rarity.legendary", 0xe2b751, EnumChatFormatting.GOLD),

    ERROR(null, 0, EnumChatFormatting.DARK_RED);

    private final String displayName;
    private final int subtitleColor;
    private final EnumChatFormatting rarityColor;

    private final ResourceLocation backResource;
    private final ResourceLocation frontResource;

    CardRarity(String displayName, int subtitleColor, EnumChatFormatting rarityColor) {
        this.displayName = displayName;
        this.subtitleColor = subtitleColor;
        this.rarityColor = rarityColor;

        this.backResource = RewardClaim
                .getGuiTexture("cards/cardback_" + name().toLowerCase() + ".png");
        this.frontResource = RewardClaim
                .getGuiTexture("cards/cardfront_" + name().toLowerCase() + ".png");
    }

    public static CardRarity fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getSubtitleColor() {
        return this.subtitleColor;
    }

    public EnumChatFormatting getRarityColor() {
        return this.rarityColor;
    }

    public ResourceLocation getBackResource() {
        return this.backResource;
    }

    public ResourceLocation getFrontResource() {
        return this.frontResource;
    }
}
