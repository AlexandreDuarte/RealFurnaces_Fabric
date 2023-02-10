package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;

public interface ServerWorldChunkEntitiesTick {

    public WorldChunkEntitiesTickScheduler<ChunkEntity> getChunkEntityTickScheduler();

}
