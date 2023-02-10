package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;

public interface ProtoChunkGlobalEntityInterface {


    public void setChunkEntitySimpleTickScheduler(ChunkEntitiesSimpleTickScheduler<ChunkEntity> globalSimpleTickScheduler);
    public BasicChunkTickScheduler<ChunkEntity> getProtoChunkEntityTickScheduler();
}
