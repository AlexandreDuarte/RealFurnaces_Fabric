package azokh99.realfurnaces.block;

import azokh99.realfurnaces.RealFurnaces;
import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import azokh99.realfurnaces.world.ChunkEntityInterface;
import azokh99.realfurnaces.world.WorldEntityInterface;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.block.*;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class FurnaceFireBlock extends BlockWithEntity implements PolymerBlock, PolymerKeepModel, PolymerClientDecoded {

    private final float damage;
    private Optional<Float> northHeat = Optional.of(0.0f), southHeat = Optional.of(0f), eastHeat = Optional.of(0f), westHeat = Optional.of(0f);
    private float upHeat = 0.0f;

    public FurnaceFireBlock(Settings settings) {
        super(settings);
        this.damage = 1.0f;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.FIRE;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.FIRE.getDefaultState();
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!entity.isFireImmune()) {
            entity.setFireTicks(entity.getFireTicks() + 1);
            if (entity.getFireTicks() == 0) {
                entity.setOnFireFor(8);
            }
        }
        entity.damage(DamageSource.IN_FIRE, this.damage);
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world.isClient()) return;


        if (oldState.isOf(state.getBlock())) {
            return;
        }
        if (!state.canPlaceAt(world, pos)) {
            world.removeBlock(pos, true);
        } else {
            world.scheduleBlockTick(pos, this, 30);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.scheduleBlockTick(pos, this, 30);
        northHeat = getNeighbourHeat(world, pos.north());
        southHeat = getNeighbourHeat(world, pos.south());
        westHeat = getNeighbourHeat(world, pos.west());
        eastHeat = getNeighbourHeat(world, pos.east());



        FurnaceBlockEntity blockEntity = ((FurnaceBlockEntity)world.getBlockEntity(pos));

        if (northHeat.isEmpty())
            northHeat = Optional.of(blockEntity.getMarginHeat());
        if (southHeat.isEmpty())
            southHeat = Optional.of(blockEntity.getMarginHeat());
        if (westHeat.isEmpty())
            westHeat = Optional.of(blockEntity.getMarginHeat());
        if (eastHeat.isEmpty())
            eastHeat = Optional.of(blockEntity.getMarginHeat());

        if (world.getBlockState(pos.up()).isOf(Blocks.BRICKS))  {
            upHeat = blockEntity.getMarginHeat();
        } else {
            upHeat = 0f;
        }

        float realizeEnergy = (blockEntity.getInternalEnergy()/FurnaceBlockEntity.getStartingInternalEnergy()) * 20f * (0.05f + blockEntity.getHeat()/(5*blockEntity.getHeat() + 100f));
        if (blockEntity.getInternalEnergy() > realizeEnergy) {
            blockEntity.removeFromInternalEnergy(realizeEnergy);
        } else {
            realizeEnergy = 0f;
        }

        blockEntity.addToMarginHeat((blockEntity.getHeat() - 2*blockEntity.getMarginHeat()) * 0.05f);
        blockEntity.addToHeat((northHeat.get() + southHeat.get() + eastHeat.get() + westHeat.get() + upHeat + blockEntity.getHeat() - 6* blockEntity.getHeat())*0.1f + realizeEnergy);
        System.out.println(blockEntity.hashCode() + ": " +  blockEntity.getHeat() + ", " + blockEntity.getMarginHeat() + ", I: " + blockEntity.getInternalEnergy() + ", " + realizeEnergy);
    }

    private Optional<Float> getNeighbourHeat(World world, BlockPos pos) {
        if (world.getBlockState(pos).isOf(RealFurnaces.FURNACE_FIRE_BLOCK)) {
            if (world.getBlockEntity(pos) != null)
                return Optional.of(((FurnaceBlockEntity) world.getBlockEntity(pos)).getHeat());
        }
        if (world.getBlockState(pos).isOf(Blocks.BRICKS)) {
            return Optional.empty();
        }
        return Optional.of(0.0f);
    }


    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (this.canPlaceAt(state, world, pos)) {
            return this.getDefaultState();
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, Direction.UP);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            world.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.CONSUME;
        }
        ItemStack stack = player.getMainHandStack();

        if (player.getActiveHand() == hand) {

            Map<Item, Integer> fuels = AbstractFurnaceBlockEntity.createFuelTimeMap();
            FurnaceBlockEntity be = ((FurnaceBlockEntity) world.getBlockEntity(pos));

            if (fuels.containsKey(stack.getItem())) {



                if (FurnaceBlockEntity.getStartingInternalEnergy() - be.getInternalEnergy() < fuels.get(stack.getItem()) / 10f) {
                    return ActionResult.FAIL;
                }

                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
                float delta = fuels.get(stack.getItem()) / 10f;

                be.addToInternalEnergy(delta);

                player.getMainHandStack().decrement(1);

                return ActionResult.SUCCESS;
            } else {
                for (ChunkEntityId ids : ((ChunkEntityInterface)world.getChunk(pos)).getChunkEntitiesId()) {
                    System.out.println("ids: " + ids.getId());
                }

                if (((ChunkEntityInterface)world.getChunk(pos)).getChunkEntity(be.getGlobalEntityId()) != null) {
                    System.out.println("previous: " + ((ChunkEntityInterface)world.getChunk(pos)).getChunkEntity(be.getGlobalEntityId()).getId());
                }
                if (!((WorldEntityInterface)world).hasChunkEntity(new ChunkPos(pos), be.getGlobalEntityId())) {
                    ChunkEntity globalEntity = new ChunkEntity(new ChunkPos(pos));
                    ((WorldEntityInterface)world).addChunkEntity(globalEntity);
                    System.out.println("NEW global Entity id: " + globalEntity.getId().getId() + ", previous: " + be.getGlobalEntityId().getId());
                    be.setGlobalEntityId(globalEntity.getId().getId());
                } else {
                    System.out.println("Global Entity of id " + be.getGlobalEntityId() + " already Exists");
                }
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FurnaceBlockEntity(pos, state);
    }
}
