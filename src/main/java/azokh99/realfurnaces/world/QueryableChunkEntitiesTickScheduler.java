package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import net.minecraft.util.math.ChunkPos;

public interface QueryableChunkEntitiesTickScheduler<T>
        extends IChunkEntitiesTickScheduler<T> {
    public boolean isTicking(T var2, ChunkEntityId id, ChunkPos pos);
}
