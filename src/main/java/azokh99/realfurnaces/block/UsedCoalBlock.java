package azokh99.realfurnaces.block;

import azokh99.realfurnaces.RealFurnaces;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Collections;
import java.util.List;

public class UsedCoalBlock extends Block implements PolymerBlock, PolymerKeepModel, PolymerClientDecoded {

    public UsedCoalBlock(Settings settings) {
        super(settings);
    }


    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.COAL_ORE;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.COAL_ORE.getDefaultState();
    }

    @Override
    public Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        return PolymerBlock.super.getPolymerBlock(state, player);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {


        if (world.isClient()) return;

        Block downBlock = world.getBlockState(pos.down()).getBlock();

        if (downBlock.getDefaultState().isOf(Blocks.BRICKS)) {
            int count = 0;

            int[] i = {0, -1, 0, 1};
            int[] k = {1, 0, -1, 0};

            for (int index = 0; index < 4; index++) {
                BlockPos neighbourPos = pos.add(i[index], 0, k[index]);
                Block block = world.getBlockState(pos.add(i[index], 0, k[index])).getBlock();
                if (block.getDefaultState().isOf(RealFurnaces.USED_COAL_BLOCK) || block.getDefaultState().isOf(Blocks.BRICKS)) {
                    count += 1;
                }
            }

            if (count < 4) {
                world.scheduleBlockTick(pos, this, 30);
            }
        } else {
            world.removeBlockEntity(pos);
            world.setBlockState(pos, Blocks.COAL_BLOCK.getDefaultState());
        }
    }


    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int count = 0;

        int[] i = {0, -1, 0, 1};
        int[] k = {1, 0, -1, 0};

        for (int index = 0; index < 4; index++) {
            BlockPos neighbourPos = pos.add(i[index], 0, k[index]);
            Block block = world.getBlockState(pos.add(i[index], 0, k[index])).getBlock();
            if (block.getDefaultState().isOf(RealFurnaces.USED_COAL_BLOCK) || block.getDefaultState().isOf(Blocks.BRICKS)) {
                count += 1;
            } else if (world.getBlockState(neighbourPos).isOf(Blocks.COAL_BLOCK)) {
                if (this.canPlaceAt(state, world, neighbourPos)) {
                    world.setBlockState(neighbourPos, RealFurnaces.USED_COAL_BLOCK.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    world.setBlockState(neighbourPos.up(), RealFurnaces.FURNACE_FIRE_BLOCK.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                }
            }
        }

        if (count < 4) {
            world.scheduleBlockTick(pos, this, 30);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        int count = 0;

        int[] i = {0, -1, 0, 1};
        int[] k = {1, 0, -1, 0};

        for (int index = 0; index < 4; index++) {
            Block block = world.getBlockState(pos.add(i[index], 0, k[index])).getBlock();
            if (block.getDefaultState().isOf(RealFurnaces.USED_COAL_BLOCK) || block.getDefaultState().isOf(Blocks.BRICKS)) {
                count += 1;
            }
        }

        if (count < 4) {
            world.scheduleBlockTick(pos, this, 30);
        }
    }

    /*@Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        super.onUse(state, world, pos, player, hand, hit);

        return ActionResult.SUCCESS;
    }*/

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        return Collections.emptyList();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            if (!neighborState.isOf(RealFurnaces.FURNACE_FIRE_BLOCK)) {
                return Blocks.CLAY.getDefaultState();
            }
        } else if (direction == Direction.DOWN) {
            if (!neighborState.isOf(Blocks.BRICKS)) {
                return Blocks.CLAY.getDefaultState();
            }
        }

        if (this.canPlaceAt(state, world, pos)) {
            return state;
        }

        return Blocks.CLAY.getDefaultState();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        return (world.getBlockState(blockPos).isOf(Blocks.BRICKS) && (world.isAir(pos.up()) || world.getBlockState(pos.up()).isOf(RealFurnaces.FURNACE_FIRE_BLOCK)));
    }

}
