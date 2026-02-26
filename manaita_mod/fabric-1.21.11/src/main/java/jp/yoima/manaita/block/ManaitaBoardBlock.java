package jp.yoima.manaita.block;

import com.mojang.serialization.MapCodec;
import jp.yoima.manaita.screen.ManaitaScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
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

public class ManaitaBoardBlock extends HorizontalFacingBlock {
    private static final VoxelShape SHAPE_Z = Block.createCuboidShape(1.0, 0.0, 3.0, 15.0, 1.0, 13.0);
    private static final VoxelShape SHAPE_X = Block.createCuboidShape(3.0, 0.0, 1.0, 13.0, 1.0, 15.0);

    private final ManaitaTier tier;

    public ManaitaBoardBlock(ManaitaTier tier, Settings settings) {
        super(settings);
        this.tier = tier;
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return MapCodec.unit(this);
    }

    public ManaitaTier getTier() {
        return this.tier;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, net.minecraft.world.BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        Direction facing = state.get(FACING);
        return facing.getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        return belowState.isSideSolidFullSquare(world, below, Direction.UP);
    }

    @Override
    public BlockState getPlacementState(net.minecraft.item.ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
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
        builder.add(FACING);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos)) {
            return net.minecraft.block.Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        NamedScreenHandlerFactory factory = this.createScreenHandlerFactory(state, world, pos);
        if (factory != null) {
            player.openHandledScreen(factory);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    protected ActionResult onUseWithItem(net.minecraft.item.ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return this.onUse(state, world, pos, player, hit);
    }

    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        int multiplier = this.tier.getMultiplier();
        Text title = Text.translatable("container.manaita", multiplier);
        return new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, player) -> new ManaitaScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos), multiplier),
                title
        );
    }
}
