package me.dancedog.rewardclaim.types;

import me.dancedog.rewardclaim.RewardClaim;
import net.minecraft.util.ResourceLocation;

/**
 * The type of reward (or base type) offered to a player
 * <p>
 * Created by DanceDog / Ben on 3/22/20 @ 9:23 PM
 */
@SuppressWarnings("unused")
public enum RewardType {
    // Generic rewards
    DUST("type.dust", "DUST"),
    EXPERIENCE("type.experience", "EXPERIENCE"),
    MYSTERY_BOX("type.mystery_box", "MYSTERY_BOX"),
    SOULS("type.souls", "SOULS"),
    GIFT_BOX("type.gift_box", "GIFT_BOX"),
    ADSENSE_TOKEN("type.adsense_token", "ADSENSE_TOKEN"),

    // Special rewards
    COINS("type.coins", "COINS"),
    TOKENS("type.tokens", null),
    HOUSING_PACKAGE("type.housing_package", null, false),
    ADD_VANITY("type.add_vanity", null, false);

    private final String title;
    private ResourceLocation icon;
    private final boolean hasAmount;

    /**
     * RewardType with hasAmount defaulting to true
     */
    RewardType(String title, String iconName) {
        this(title, iconName, true);
    }

    RewardType(String title, String iconName, boolean hasAmount) {
        this.title = title;
        this.hasAmount = hasAmount;

        if (iconName != null) {
            this.icon = RewardClaim.getGuiTexture("reward_base/" + iconName + ".png");
        }
    }

    public boolean hasAmount() {
        return this.hasAmount;
    }

    public static RewardType fromName(String name) {
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

    public String getTitle() {
        return this.title;
    }

    public ResourceLocation getIcon() {
        return this.icon;
    }
}
