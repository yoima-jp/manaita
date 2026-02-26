package jp.yoima.manaita.screen;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ManaitaScreenHandler extends CraftingScreenHandler {
    private static final int PLAYER_SLOT_START = 10;
    private static final String LOCKED_OVERSTACK_KEY = "manaita_locked_overstack";
    private final int multiplier;
    private final ScreenHandlerContext context;

    public ManaitaScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, 1);
    }

    public ManaitaScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, int multiplier) {
        super(syncId, playerInventory, context);
        this.context = context;
        this.multiplier = Math.max(1, multiplier);
        this.upgradePlayerSlotsForLockedOverstack();
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        this.applyMultiplierToResultAndSync();
    }

    @Override
    public void onInputSlotFillFinish(ServerWorld world, RecipeEntry<CraftingRecipe> recipe) {
        super.onInputSlotFillFinish(world, recipe);
        this.applyMultiplierToResultAndSync();
    }

    private void applyMultiplierToResultAndSync() {
        this.context.run((world, pos) -> {
            if (!(world instanceof ServerWorld)) {
                return;
            }
            if (!(this.getPlayer() instanceof ServerPlayerEntity serverPlayer)) {
                return;
            }

            Slot resultSlot = this.getSlot(CraftingScreenHandler.RESULT_ID);
            if (!resultSlot.hasStack()) {
                return;
            }

            ItemStack stack = resultSlot.getStack();
            long multipliedLong = (long) stack.getCount() * (long) this.getMultiplier();
            int multipliedCount = multipliedLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) multipliedLong;
            if (multipliedCount < 1) {
                multipliedCount = 1;
            }
            if (stack.getCount() == multipliedCount) {
                return;
            }

            ItemStack multiplied = stack.copyWithCount(multipliedCount);
            if (multipliedCount > stack.getMaxCount()) {
                NbtComponent.set(DataComponentTypes.CUSTOM_DATA, multiplied, nbt -> nbt.putBoolean(LOCKED_OVERSTACK_KEY, true));
            }
            this.getOutputSlot().setStack(multiplied);
            this.setReceivedStack(CraftingScreenHandler.RESULT_ID, multiplied.copy());
            serverPlayer.networkHandler.sendPacket(
                    new ScreenHandlerSlotUpdateS2CPacket(
                            this.syncId,
                            this.nextRevision(),
                            CraftingScreenHandler.RESULT_ID,
                            multiplied
                    )
            );
        });
    }

    private void upgradePlayerSlotsForLockedOverstack() {
        for (int slotIndex = PLAYER_SLOT_START; slotIndex < this.slots.size(); slotIndex++) {
            Slot original = this.slots.get(slotIndex);
            Slot replacement = new LockedOverstackAwareSlot(original.inventory, original.getIndex(), original.x, original.y);
            replacement.id = original.id;
            this.slots.set(slotIndex, replacement);
        }
    }

    @Override
    public boolean canUse(net.minecraft.entity.player.PlayerEntity player) {
        final boolean[] canUse = {true};
        this.context.run((world, pos) -> {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            canUse[0] = player.squaredDistanceTo(x, y, z) <= 64.0;
        });
        return canUse[0];
    }

    private static boolean isLockedOverstack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return false;
        }
        if (!customData.copyNbt().getBoolean(LOCKED_OVERSTACK_KEY, false)) {
            return false;
        }
        return stack.getCount() > stack.getMaxCount();
    }

    private static final class LockedOverstackAwareSlot extends Slot {
        private LockedOverstackAwareSlot(net.minecraft.inventory.Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxItemCount() {
            ItemStack current = this.getStack();
            if (isLockedOverstack(current)) {
                return Math.max(1, current.getCount());
            }
            return super.getMaxItemCount();
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            ItemStack current = this.getStack();
            if (isLockedOverstack(current)) {
                return Math.max(1, current.getCount());
            }
            if (isLockedOverstack(stack)) {
                return Math.max(1, stack.getCount());
            }
            return super.getMaxItemCount(stack);
        }
    }
}
