package jp.yoima.manaita.block;

import net.minecraft.item.ItemStack;

public enum ManaitaTier {
    WOODEN("wooden", 2),
    STONE("stone", 4),
    IRON("iron", 8),
    GOLD("gold", 16),
    DIAMOND("diamond", 32);

    private final String id;
    private final int multiplier;

    ManaitaTier(String id, int multiplier) {
        this.id = id;
        this.multiplier = multiplier;
    }

    public String getId() {
        return this.id;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public static ManaitaTier fromBoardStack(ItemStack stack) {
        return jp.yoima.manaita.registry.ModBlocks.tierForBoard(stack);
    }
}
