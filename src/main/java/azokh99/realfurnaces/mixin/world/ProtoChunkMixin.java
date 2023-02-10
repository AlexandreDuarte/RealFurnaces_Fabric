package azokh99.realfurnaces.mixin.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.world.*;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin extends ChunkMixin implements ProtoChunkGlobalEntityInterface {

    @Unique
    private ChunkEntitiesSimpleTickScheduler<ChunkEntity> chunkEntityScheduler;

    @Unique
    private static <T> ChunkEntitiesTickScheduler<T> createGlobalProtoTickScheduler(ChunkEntitiesSimpleTickScheduler<T> tickScheduler) {
        return new ChunkEntitiesTickScheduler<T>(tickScheduler.getTicks());
    }

    @Inject(
            method = "<init>(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/UpgradeData;[Lnet/minecraft/world/chunk/ChunkSection;Lnet/minecraft/world/tick/SimpleTickScheduler;Lnet/minecraft/world/tick/SimpleTickScheduler;Lnet/minecraft/world/HeightLimitView;Lnet/minecraft/registry/Registry;Lnet/minecraft/world/gen/chunk/BlendingData;)V",
            at = @At("TAIL")
    )
    public void initWorldChunk(CallbackInfo ci) {
        this.chunkEntityScheduler = new ChunkEntitiesSimpleTickScheduler<ChunkEntity>();
    }


    @Unique
    @Override
    public void setChunkEntitySimpleTickScheduler(ChunkEntitiesSimpleTickScheduler<ChunkEntity> globalSimpleTickScheduler) {
        this.chunkEntityScheduler = globalSimpleTickScheduler;
    }

    @Unique
    @Override
    public ChunkEntitiesTickScheduler<ChunkEntity> getProtoChunkEntityTickScheduler() {
        return ProtoChunkMixin.createGlobalProtoTickScheduler(this.chunkEntityScheduler);
    }

    @Override
    public ChunkEntitiesTickSchedulers getChunkEntityTickSchedulers() {
        return new ChunkEntitiesTickSchedulers(this.chunkEntityScheduler);
    }

}
