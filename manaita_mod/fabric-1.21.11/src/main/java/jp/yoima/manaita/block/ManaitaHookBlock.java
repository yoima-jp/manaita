package jp.yoima.manaita.block;

import com.mojang.serialization.MapCodec;
import jp.yoima.manaita.ManaitaMod;
import jp.yoima.manaita.registry.ModBlocks;
import jp.yoima.manaita.screen.ManaitaScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class ManaitaHookBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<HookBoardState> BOARD = EnumProperty.of("board", HookBoardState.class);

    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(3.0, 1.0, 11.0, 13.0, 15.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(3.0, 1.0, 0.0, 13.0, 15.0, 5.0);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(11.0, 1.0, 3.0, 16.0, 15.0, 13.0);
    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0.0, 1.0, 3.0, 5.0, 15.0, 13.0);

    public ManaitaHookBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(BOARD, HookBoardState.EMPTY));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return MapCodec.unit(this);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, net.minecraft.world.BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        for (Direction direction : ctx.getPlacementDirections()) {
            if (!direction.getAxis().isHorizontal()) {
                continue;
            }
            BlockState state = this.getDefaultState().with(FACING, direction.getOpposite());
            if (state.canPlaceAt(world, pos)) {
                return state;
            }
        }
        return null;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, BOARD);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        BlockState supportState = world.getBlockState(supportPos);
        return supportState.isSideSolidFullSquare(world, supportPos, facing);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (direction == state.get(FACING).getOpposite() && !this.canPlaceAt(state, world, pos)) {
            return net.minecraft.block.Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return this.handleInteraction(state, world, pos, player, player.getStackInHand(Hand.MAIN_HAND));
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return this.handleInteraction(state, world, pos, player, stack);
    }

    private ActionResult handleInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack heldStack) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        HookBoardState boardState = state.get(BOARD);
        if (!boardState.hasBoard() && ModBlocks.isBoardItem(heldStack)) {
            HookBoardState insertedBoard = HookBoardState.fromStack(heldStack);
            world.setBlockState(pos, state.with(BOARD, insertedBoard), Block.NOTIFY_ALL);
            if (!player.isCreative()) {
                heldStack.decrement(1);
            }
            return ActionResult.CONSUME;
        }

        if (!boardState.hasBoard()) {
            return ActionResult.PASS;
        }

        if (player.isSneaking()) {
            ItemStack removed = new ItemStack(ModBlocks.itemFor(boardState.getTier()));
            world.setBlockState(pos, state.with(BOARD, HookBoardState.EMPTY), Block.NOTIFY_ALL);
            if (!removed.isEmpty()) {
                giveBoardBackToPlayer(player, removed.copy());
            }
            return ActionResult.CONSUME;
        }

        NamedScreenHandlerFactory factory = this.createScreenHandlerFactory(state, world, pos);
        if (factory != null) {
            player.openHandledScreen(factory);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    private static void giveBoardBackToPlayer(PlayerEntity player, ItemStack stack) {
        PlayerInventory inventory = player.getInventory();
        if (!inventory.insertStack(stack.copy())) {
            player.dropItem(stack, false);
        }
    }

    @Override
    @Nullable
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        HookBoardState boardState = state.get(BOARD);
        if (!boardState.hasBoard()) {
            return null;
        }

        int multiplier = boardState.getTier().getMultiplier();
        return new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, player) -> new ManaitaScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos), multiplier),
                Text.translatable("container.manaita", multiplier)
        );
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        HookBoardState boardState = state.get(BOARD);
        if (boardState.hasBoard()) {
            Item boardItem = ModBlocks.itemFor(boardState.getTier());
            if (boardItem != null) {
                net.minecraft.util.ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(boardItem));
            }
        }
        super.onStateReplaced(state, world, pos, moved);
    }
}
