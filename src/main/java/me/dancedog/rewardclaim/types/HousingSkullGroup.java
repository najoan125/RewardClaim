package me.dancedog.rewardclaim.types;

import me.dancedog.rewardclaim.RewardClaim;
import net.minecraft.util.ResourceLocation;

/**
 * Which color group a housing block belongs to (red/green/blue) This determines the card's main
 * icon
 * <p>
 * Created by DanceDog / Ben on 3/23/20 @ 4:51 PM
 */
@SuppressWarnings("unused")
public enum HousingSkullGroup {
    RED,
    GREEN,
    BLUE;

    private final ResourceLocation resource;

    HousingSkullGroup() {
        this.resource = RewardClaim.getGuiTexture("reward_base/HOUSING_" + name() + ".png");
    }

    public ResourceLocation getResource() {
        return this.resource;
    }
}
