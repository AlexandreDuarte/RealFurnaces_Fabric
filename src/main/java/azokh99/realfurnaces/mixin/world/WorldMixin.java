package azokh99.realfurnaces.mixin.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import azokh99.realfurnaces.world.ChunkEntityInterface;
import azokh99.realfurnaces.world.ChunkEntityTickInvoker;
import azokh99.realfurnaces.world.WorldEntityInterface;
import azokh99.realfurnaces.world.WrappedChunkEntityTickInvoker;
import com.google.common.collect.Lists;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import java.util.List;

@Mixin(World.class)
public abstract class WorldMixin implements WorldEntityInterface {

    private final List<ChunkEntityTickInvoker> globalEntityTickers = Lists.newArrayList();


    private final List<ChunkEntityTickInvoker> pendingGlobalEntityTickers = Lists.newArrayList();

    @Shadow
    private boolean iteratingTickingBlockEntities;

    @Unique
    public void addChunkEntityTicker(WrappedChunkEntityTickInvoker wrappedBlockEntityTickInvoker) {
        (this.iteratingTickingBlockEntities ? this.pendingGlobalEntityTickers : this.globalEntityTickers).add(wrappedBlockEntityTickInvoker);
    }

    @Unique
    public void addChunkEntity(ChunkEntity chunkEntity) {
        ChunkPos chunkEntityPos = chunkEntity.getPos();
        ChunkSectionPos pos = ChunkSectionPos.from(chunkEntityPos, 0);
        ((ChunkEntityInterface)((World)(Object)this).getChunk(pos.getSectionX(), pos.getSectionZ())).addChunkEntity(chunkEntity);
    }

    public boolean hasChunkEntity(ChunkPos chunkPos, ChunkEntityId id) {
        ChunkSectionPos pos = ChunkSectionPos.from(chunkPos, 0);
        ChunkEntity globalEntity = ((ChunkEntityInterface)((World)(Object)this).getChunk(pos.getSectionX(), pos.getSectionZ())).getChunkEntity(id);
        if (globalEntity == null) {
            return false;
        }
        return true;
    }

    @Unique
    protected void tickChunkEntities() {
        Profiler profiler = ((World)(Object)this).getProfiler();
        profiler.push("chunk_entities");
        this.iteratingTickingBlockEntities = true;
        if (!this.pendingGlobalEntityTickers.isEmpty()) {
            this.globalEntityTickers.addAll(this.pendingGlobalEntityTickers);
            this.pendingGlobalEntityTickers.clear();
        }

        Iterator<ChunkEntityTickInvoker> iterator = this.globalEntityTickers.iterator();

        while(iterator.hasNext()) {
            ChunkEntityTickInvoker globalEntityTickInvoker = (ChunkEntityTickInvoker)iterator.next();
            if (globalEntityTickInvoker.isRemoved()) {
                iterator.remove();
            } else if (((World)(Object)this).shouldTickBlocksInChunk(globalEntityTickInvoker.getPos().toLong())) {
                globalEntityTickInvoker.tick();
            }
        }

        this.iteratingTickingBlockEntities = false;
        profiler.pop();
    }

}
