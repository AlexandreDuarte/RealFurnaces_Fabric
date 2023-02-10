package azokh99.realfurnaces.mixin.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import azokh99.realfurnaces.world.ServerWorldChunkEntitiesTick;
import azokh99.realfurnaces.world.WorldChunkEntitiesTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends WorldMixin implements ServerWorldChunkEntitiesTick {

    @Unique
    @Final
    private final WorldChunkEntitiesTickScheduler<ChunkEntity> chunkEntityTickScheduler = new WorldChunkEntitiesTickScheduler(this::isTickingFutureReady, ((ServerWorld)(Object)this).getProfilerSupplier());

    @Shadow
    private boolean isTickingFutureReady(long chunkPos) {
        return false;
    }

    /*@Inject(method = "<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionOptions;Lnet/minecraft/server/WorldGenerationProgressListener;ZJLjava/util/List;Z)V", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        this.globalTickScheduler = new WorldGlobalTickScheduler(this::isTickingFutureReady, ((ServerWorld)(Object)this).getProfilerSupplier());
    }*/

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V",
                    shift = At.Shift.BEFORE),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getTime()J"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V")
            ),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injected_tick_1(BooleanSupplier shouldKeepTicking, CallbackInfo ci, Profiler profiler, int i, long l) {
        profiler.swap("chunk_ticks");
        this.chunkEntityTickScheduler.tick(l, 65536, this::tickChunk);
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V", shift = At.Shift.AFTER))
    private void injected_tick_2(CallbackInfo ci) {
        this.tickChunkEntities();
    }

    @Unique
    private void tickChunk(ChunkEntityId id, ChunkEntity globalEntity) {
        globalEntity.scheduledTick(((ServerWorld)(Object)this), id, ((ServerWorld)(Object)this).random);
    }

    @Unique
    public WorldChunkEntitiesTickScheduler<ChunkEntity> getChunkEntityTickScheduler() {
        return this.chunkEntityTickScheduler;
    }




}
