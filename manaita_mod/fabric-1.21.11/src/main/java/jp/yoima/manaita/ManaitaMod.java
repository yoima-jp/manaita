package jp.yoima.manaita;

import java.util.List;
import jp.yoima.manaita.registry.ModBlocks;
import jp.yoima.manaita.registry.ModItemGroups;
import jp.yoima.manaita.registry.ModItems;
import jp.yoima.manaita.registry.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManaitaMod implements ModInitializer {
    public static final String MOD_ID = "manaita";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ManaitaConfig CONFIG;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CONFIG = ManaitaConfig.load();
        ModItems.register();
        ModBlocks.register();
        ModScreenHandlers.register();
        ModItemGroups.register();
        registerEvents();
        LOGGER.info("Initialized Manaita Fabric for MC 1.21.11");
    }

    private static void registerEvents() {
        PlayerBlockBreakEvents.AFTER.register(ManaitaMod::onBlockBreakAfter);
    }

    private static void onBlockBreakAfter(World world, net.minecraft.entity.player.PlayerEntity player, BlockPos pos, BlockState state, net.minecraft.block.entity.BlockEntity blockEntity) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        if (!state.isOf(Blocks.OAK_PLANKS)) {
            return;
        }

        float chance = MathHelper.clamp(CONFIG.plateDropChance, 0.0f, 1.0f);
        Random random = serverWorld.getRandom();
        if (random.nextFloat() >= chance) {
            return;
        }

        Box box = new Box(pos).expand(0.9);
        List<ItemEntity> droppedPlanks = serverWorld.getEntitiesByClass(
                ItemEntity.class,
                box,
                entity -> entity.isAlive()
                        && entity.age <= 2
                        && entity.getStack().isOf(net.minecraft.item.Items.OAK_PLANKS)
        );
        for (ItemEntity entity : droppedPlanks) {
            entity.discard();
        }

        net.minecraft.util.ItemScatterer.spawn(
                serverWorld,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                new ItemStack(ModItems.PLATE)
        );
    }
}
