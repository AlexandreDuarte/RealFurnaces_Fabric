package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import net.minecraft.nbt.NbtCompound;

import java.util.Set;

public interface ChunkEntityInterface {
    public void addChunkEntity(ChunkEntity chunkEntity);
    public int findAvailableChunkEntityId();
    public void addPendingChunkEntityNbt(NbtCompound nbt);
    public ChunkEntitiesTickSchedulers getChunkEntityTickSchedulers();
    public ChunkEntity getChunkEntity(ChunkEntityId id);
    public Set<ChunkEntityId> getChunkEntitiesId();
    public abstract NbtCompound getPackedChunkEntityNbt(ChunkEntityId id);
    void setChunkEntity(ChunkEntity ge);
}
