package azokh99.realfurnaces.mixin.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.world.ChunkEntityInterface;
import azokh99.realfurnaces.world.ChunkEntitiesTickSchedulers;
import azokh99.realfurnaces.world.ChunkEntitiesSimpleTickScheduler;
import azokh99.realfurnaces.world.ProtoChunkGlobalEntityInterface;
import com.mojang.serialization.Codec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.tick.SimpleTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @Inject(
            method = "deserialize(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/poi/PointOfInterestStorage;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/world/chunk/ProtoChunk;",
            at = @At(
                     value = "TAIL",
                     shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void injected_deserializeChunkEntities(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir) {
        NbtList nbtListGE = nbt.getList("chunk_entities", NbtElement.COMPOUND_TYPE);
        for (int p = 0; p < nbtListGE.size(); ++p) {
            NbtCompound nbtCompoundGE = nbtListGE.getCompound(p);
            ((ChunkEntityInterface)cir.getReturnValue()).addPendingChunkEntityNbt(nbtCompoundGE);
        }
    }

    @Inject(
            method = "serialize(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;)Lnet/minecraft/nbt/NbtCompound;",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void injected_serializeChunkEntities(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir, ChunkPos chunkPos, NbtCompound nbtCompound, BlendingData blendingData, BelowZeroRetrogen belowZeroRetrogen, UpgradeData upgradeData, ChunkSection[] chunkSections, NbtList nbtList, LightingProvider lightingProvider, Registry registry, Codec codec, boolean bl, NbtList nbtList2, NbtCompound nbtCompound4) {
        NbtList nbtListGE = new NbtList();
        for (ChunkEntityId geid : ((ChunkEntityInterface)chunk).getChunkEntitiesId()) {
            NbtCompound nbtCompoundGE = ((ChunkEntityInterface)chunk).getPackedChunkEntityNbt(geid);
            if (nbtCompoundGE == null) continue;
            nbtListGE.add(nbtCompoundGE);
        }
        nbtCompound.put("chunk_entities", nbtListGE);
    }

    @Inject(
            method = "serialize",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/ChunkSerializer;serializeTicks(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/world/chunk/Chunk$TickSchedulers;)V",
                     shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void injected_serializeChunkTicks(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> cir, ChunkPos chunkPos, NbtCompound nbtCompound) {
        long l = world.getLevelProperties().getTime();
        ChunkEntitiesTickSchedulers tickScheduler = ((ChunkEntityInterface)chunk).getChunkEntityTickSchedulers();
        nbtCompound.put("chunk_ticks", tickScheduler.globals().toNbt(l, global -> global.getId().toString()));
    }

    /*@Inject(
            method = "deserialize(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/poi/PointOfInterestStorage;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/world/chunk/ProtoChunk;",
            at = @At(
                    value = "NEW",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;Lnet/minecraft/world/tick/ChunkTickScheduler;Lnet/minecraft/world/tick/ChunkTickScheduler;J[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void deserialize_WorldChunkCreation(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir, ChunkPos chunkPos2, UpgradeData upgradeData, boolean bl, NbtList nbtList, int i, ChunkSection[] chunkSections, boolean bl2, ChunkManager chunkManager, LightingProvider lightingProvider, Registry registry, Codec codec, boolean bl3, long m, ChunkStatus.ChunkType chunkType, BlendingData blendingData, boolean bl4, ChunkTickScheduler chunkTickScheduler, ChunkTickScheduler chunkTickScheduler2) {

    }*/

    @Inject(
            method = "deserialize(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/poi/PointOfInterestStorage;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/world/chunk/ProtoChunk;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;setInhabitedTime(J)V",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void deserialize_ProtoChunkCreation(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos, NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> cir, ChunkPos chunkPos2, UpgradeData upgradeData, boolean bl, NbtList nbtList, int i, ChunkSection[] chunkSections, boolean bl2, ChunkManager chunkManager, LightingProvider lightingProvider, Registry registry, Codec codec, boolean bl3, long m, ChunkStatus.ChunkType chunkType, BlendingData blendingData, Chunk chunk, SimpleTickScheduler simpleTickScheduler, SimpleTickScheduler simpleTickScheduler2, ProtoChunk protoChunk) {
        ChunkEntitiesSimpleTickScheduler<ChunkEntity> simpleGlobalTickScheduler = ChunkEntitiesSimpleTickScheduler.tick(nbt.getList("chunk_ticks", NbtElement.COMPOUND_TYPE), id -> Optional.of(new ChunkEntity(chunkPos)), chunkPos);
        ((ProtoChunkGlobalEntityInterface)protoChunk).setChunkEntitySimpleTickScheduler(simpleGlobalTickScheduler);
    }

    @Shadow
    @Nullable
    private static NbtList getList(NbtCompound nbt, String key) {
        NbtList nbtList = nbt.getList(key, NbtElement.COMPOUND_TYPE);
        return nbtList.isEmpty() ? null : nbtList;
    }

    @Redirect(
            method = "deserialize(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/poi/PointOfInterestStorage;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/world/chunk/ProtoChunk;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/ChunkSerializer;getEntityLoadingCallback(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;"
            )
    )
    private static WorldChunk.EntityLoader redirected_getEntityLoadingCallback(ServerWorld world, NbtCompound nbt) {
        NbtList nbtList = ChunkSerializerMixin.getList(nbt, "entities");
        NbtList nbtList2 = ChunkSerializerMixin.getList(nbt, "block_entities");
        NbtList nbtList3 = ChunkSerializerMixin.getList(nbt, "chunk_entities");
        if (nbtList == null && nbtList2 == null && nbtList3 == null) {
            return null;
        }
        return chunk -> {
            if (nbtList != null) {
                world.loadEntities(EntityType.streamFromNbt(nbtList, world));
            }
            if (nbtList2 != null) {
                for (int i = 0; i < nbtList2.size(); ++i) {
                    NbtCompound nbtCompound = nbtList2.getCompound(i);
                    boolean bl = nbtCompound.getBoolean("keepPacked");
                    if (bl) {
                        chunk.addPendingBlockEntityNbt(nbtCompound);
                        continue;
                    }
                    BlockPos blockPos = BlockEntity.posFromNbt(nbtCompound);
                    BlockEntity blockEntity = BlockEntity.createFromNbt(blockPos, chunk.getBlockState(blockPos), nbtCompound);
                    if (blockEntity == null) continue;
                    chunk.setBlockEntity(blockEntity);
                }
            }
            if (nbtList3 != null) {
                for (int i = 0; i < nbtList3.size(); ++i) {
                    NbtCompound nbtCompound = nbtList3.getCompound(i);
                    boolean bl = nbtCompound.getBoolean("keepPacked");
                    if (bl) {
                        chunk.addPendingBlockEntityNbt(nbtCompound);
                        continue;
                    }
                    ChunkEntity ge = ChunkEntity.createFromNbt(nbtCompound);
                    if (ge == null) continue;
                    ((ChunkEntityInterface)chunk).setChunkEntity(ge);
                }
            }
        };
    }
}
