package azokh99.realfurnaces.mixin.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.world.*;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends ChunkMixin {

    @Shadow
    static final Logger LOGGER = LogUtils.getLogger();

    @Unique
    private static final ChunkEntityTickInvoker EMPTY_GLOBAL_ENTITY_TICKER = new ChunkEntityTickInvoker() {
        @Override
        public void tick() {

        }

        @Override
        public boolean isRemoved() {
            return true;
        }

        @Override
        public ChunkPos getPos() {
            return ChunkPos.ORIGIN;
        }

        @Override
        public String getName() {
            return "<null>";
        }
    };

    @Shadow
    public abstract World getWorld();


    @Unique
    private final Map<ChunkEntityId, WrappedChunkEntityTickInvoker> chunkEntityTickers = Maps.newHashMap();


    @Unique
    private ChunkEntitiesTickScheduler<ChunkEntity> chunkTickScheduler = new ChunkEntitiesTickScheduler<>();

    @Unique
    public BasicChunkTickScheduler<ChunkEntity> getBlockTickScheduler() {
        return this.chunkTickScheduler;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At("TAIL")
    )
    public void initWorldChunk(CallbackInfo ci) {
        this.chunkTickScheduler = new ChunkEntitiesTickScheduler<>();
    }


    @Inject(method = "clear()V", at = @At("TAIL"))
    private void injected_clear(CallbackInfo ci) {
        ((ChunkMixin)this).chunkEntities.values().forEach(ChunkEntity::markRemoved);
        ((ChunkMixin)this).chunkEntities.clear();
        this.chunkEntityTickers.values().forEach(ticker -> ticker.setWrapped(EMPTY_GLOBAL_ENTITY_TICKER));
        this.chunkEntityTickers.clear();
    }

    @Unique
    private void removeGlobalEntityTicker(ChunkEntityId id) {
        WrappedChunkEntityTickInvoker wrappedBlockEntityTickInvoker = this.chunkEntityTickers.remove(id);
        if (wrappedBlockEntityTickInvoker != null) {
            wrappedBlockEntityTickInvoker.setWrapped(EMPTY_GLOBAL_ENTITY_TICKER);
        }
    }

    @Inject(method = "addChunkTickSchedulers(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
    public void injected_addChunkTickSchedulers(CallbackInfo ci) {
        ((WorldChunkEntitiesTickScheduler)((ServerWorldChunkEntitiesTick)((WorldChunkMixin)(Object)this).getWorld()).getChunkEntityTickScheduler()).addChunkTickScheduler(((WorldChunk)(Object)this).getPos(), this.chunkTickScheduler);
    }
    @Inject(method = "removeChunkTickSchedulers(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
    public void injected_removeChunkTickSchedulers(CallbackInfo ci) {
        ((WorldChunkEntitiesTickScheduler)((ServerWorldChunkEntitiesTick)((WorldChunkMixin)(Object)this).getWorld()).getChunkEntityTickScheduler()).removeChunkTickScheduler(((WorldChunk)(Object)this).getPos());
    }

    @Inject(method = "disableTickSchedulers(J)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void disableGlobalTickScheduler(long time, CallbackInfo ci) {
        this.chunkTickScheduler.disable(time);
    }

    @Unique
    public void setChunkEntity(ChunkEntity globalEntity) {
        globalEntity.setWorld(((WorldChunk)(Object)this).getWorld());
        globalEntity.cancelRemoval();
        globalEntity.setId(new ChunkEntityId(this.findAvailableChunkEntityId()));
        ChunkEntity chunkEntity2 = this.chunkEntities.put(globalEntity.getId(), globalEntity);
        if (chunkEntity2 != null && chunkEntity2 != globalEntity) {
            chunkEntity2.markRemoved();
        }
    }

    @Shadow
    private boolean canTickBlockEntities() {
        return true;
    }

    @Unique
    @Nullable
    public ChunkEntity getChunkEntity(ChunkEntityId id) {
        return this.getGlobalEntity(id, WorldChunk.CreationType.CHECK);
    }

    @Unique
    @Nullable
    public ChunkEntity getGlobalEntity(ChunkEntityId id, WorldChunk.CreationType creationType) {
        ChunkEntity globalEntity2;
        NbtCompound nbtCompound;
        ChunkEntity globalEntity = (ChunkEntity)this.chunkEntities.get(id);
        System.out.println(globalEntity);
        if (globalEntity == null && (nbtCompound = (NbtCompound)this.chunkEntityNbts.remove(id)) != null && (globalEntity2 = this.loadChunkEntity(id, nbtCompound)) != null) {
            return globalEntity2;
        }
        if (globalEntity == null) {
            return null;
        } else if (globalEntity.isRemoved()) {
            this.chunkEntities.remove(id);
            return null;
        }
        return globalEntity;
    }

    @Unique
    @Override
    @Nullable
    public NbtCompound getPackedChunkEntityNbt(ChunkEntityId id) {
        ChunkEntity globalEntity = this.getChunkEntity(id);
        if (globalEntity != null && !globalEntity.isRemoved()) {
            NbtCompound nbtCompound = globalEntity.createNbtWithIdentifyingData();
            nbtCompound.putBoolean("keepPacked", false);
            return nbtCompound;
        }
        NbtCompound nbtCompound = (NbtCompound)this.chunkEntityNbts.get(id);
        if (nbtCompound != null) {
            nbtCompound = nbtCompound.copy();
            nbtCompound.putBoolean("keepPacked", true);
        }
        return nbtCompound;
    }

    @Unique
    public void removeGlobalEntity(ChunkEntityId id) {
        ChunkEntity globalEntity;
        if (this.canTickBlockEntities() && (globalEntity = (ChunkEntity)this.chunkEntities.remove(id)) != null) {
            World world = ((WorldChunk)(Object)this).getWorld();
            /*if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                this.removeGameEventListener(blockEntity, serverWorld);
            }*/
            globalEntity.markRemoved();
        }
        this.removeGlobalEntityTicker(id);
    }

    @Unique
    public void addChunkEntity(ChunkEntity chunkEntity) {
        this.setChunkEntity(chunkEntity);
        if (this.canTickBlockEntities()) {
            World world = ((WorldChunk)(Object)this).getWorld();
            /*if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                this.updateGameEventListener(chunkEntity, serverWorld);
            }*/
            this.updateGlobalTicker(chunkEntity);
        }
    }

    @Unique
    private <T extends ChunkEntity> void updateGlobalTicker(T chunkEntity) {
        ChunkEntityTicker<T> chunkEntityTicker = chunkEntity.getTicker(((WorldChunk)(Object)this).getWorld()/*, chunkEntity.getType()*/);
        if (chunkEntityTicker == null) {
            this.removeGlobalEntityTicker(chunkEntity.getId());
        } else {
            this.chunkEntityTickers.compute(chunkEntity.getId(), (id, ticker) -> {
                ChunkEntityTickInvoker blockEntityTickInvoker = this.wrapChunkTicker(chunkEntity, chunkEntityTicker);
                if (ticker != null) {
                    ticker.setWrapped(blockEntityTickInvoker);
                    return ticker;
                }
                if (this.canTickBlockEntities()) {
                    WrappedChunkEntityTickInvoker wrappedChunkEntityTickInvoker = new WrappedChunkEntityTickInvoker(blockEntityTickInvoker);
                    ((WorldEntityInterface)((WorldChunk)(Object)this).getWorld()).addChunkEntityTicker(wrappedChunkEntityTickInvoker);
                    return wrappedChunkEntityTickInvoker;
                }
                return null;
            });
        }
    }

    @Unique
    @Nullable
    private ChunkEntity loadChunkEntity(ChunkEntityId id, NbtCompound nbt) {
        ChunkEntity chunkEntity = ChunkEntity.createFromNbt(nbt);
        if (chunkEntity != null) {
            chunkEntity.setWorld(((WorldChunk)(Object)this).getWorld());
            this.addChunkEntity(chunkEntity);
        } else {
            LOGGER.warn("Tried to load the global entity {} but failed at Chunk {}", (Object)chunkEntity.getId(), (Object)chunkEntity.getPos());
        }
        return chunkEntity;
    }

    @Unique
    public Map<ChunkEntityId, ChunkEntity> getChunkEntities() {
        return this.chunkEntities;
    }

    @Unique
    private <T extends ChunkEntity> ChunkEntityTickInvoker wrapChunkTicker(T chunkEntity, ChunkEntityTicker<T> chunkEntityTicker) {
        return new DirectChunkEntityTickInvoker(((WorldChunk)(Object)this), chunkEntity, chunkEntityTicker);
    }

    @Override
    public ChunkEntitiesTickSchedulers getChunkEntityTickSchedulers() {
        return new ChunkEntitiesTickSchedulers(this.chunkTickScheduler);
    }
}

