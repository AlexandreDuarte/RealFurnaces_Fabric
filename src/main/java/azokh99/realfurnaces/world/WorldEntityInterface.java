package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import net.minecraft.util.math.ChunkPos;

public interface WorldEntityInterface {
    public void addChunkEntity(ChunkEntity chunkEntity);
    public void addChunkEntityTicker(WrappedChunkEntityTickInvoker wrappedBlockEntityTickInvoker);
    public boolean hasChunkEntity(ChunkPos chunkPos, ChunkEntityId id);
    }
