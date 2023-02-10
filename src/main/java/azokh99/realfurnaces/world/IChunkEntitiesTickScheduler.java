package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import net.minecraft.util.math.ChunkPos;

public interface IChunkEntitiesTickScheduler<T> {

    public void scheduleTick(IdOrderedTick<T> var1);

    public boolean isQueued(T var2, ChunkEntityId id, ChunkPos pos);

    public int getTickCount();
}
