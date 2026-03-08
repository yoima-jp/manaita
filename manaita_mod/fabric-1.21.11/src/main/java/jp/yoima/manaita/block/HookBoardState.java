package jp.yoima.manaita.block;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StringIdentifiable;

public enum HookBoardState implements StringIdentifiable {
    EMPTY("empty", null),
    WOODEN("wooden", ManaitaTier.WOODEN),
    STONE("stone", ManaitaTier.STONE),
    IRON("iron", ManaitaTier.IRON),
    GOLD("gold", ManaitaTier.GOLD),
    DIAMOND("diamond", ManaitaTier.DIAMOND);

    private final String id;
    private final ManaitaTier tier;

    HookBoardState(String id, ManaitaTier tier) {
        this.id = id;
        this.tier = tier;
    }

    public static HookBoardState fromTier(ManaitaTier tier) {
        if (tier == null) {
            return EMPTY;
        }
        return switch (tier) {
            case WOODEN -> WOODEN;
            case STONE -> STONE;
            case IRON -> IRON;
            case GOLD -> GOLD;
            case DIAMOND -> DIAMOND;
        };
    }

    public static HookBoardState fromStack(ItemStack stack) {
        return fromTier(ManaitaTier.fromBoardStack(stack));
    }

    public ManaitaTier getTier() {
        return this.tier;
    }

    public boolean hasBoard() {
        return this.tier != null;
    }

    @Override
    public String asString() {
        return this.id;
    }
}
