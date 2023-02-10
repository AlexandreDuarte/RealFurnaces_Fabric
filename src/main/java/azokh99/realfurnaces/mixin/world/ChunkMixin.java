package azokh99.realfurnaces.mixin.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.world.ChunkEntityInterface;
import azokh99.realfurnaces.world.ChunkEntitiesTickSchedulers;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(Chunk.class)
public abstract class ChunkMixin
        implements BlockView,
        BiomeAccess.Storage,
        StructureHolder, ChunkEntityInterface {


    protected final Map<ChunkEntityId, ChunkEntity> chunkEntities = Maps.newHashMap();

    protected final Map<ChunkEntityId, NbtCompound> chunkEntityNbts = Maps.newHashMap();

    @Unique
    public int findAvailableChunkEntityId() {
        return Math.max(chunkEntityNbts.keySet().stream().mapToInt(ChunkEntityId::getId).max().orElse(0), chunkEntities.keySet().stream().mapToInt(ChunkEntityId::getId).max().orElse(0)) + 1;
    }

    @Unique
    public void addPendingChunkEntityNbt(NbtCompound nbt) {
        this.chunkEntityNbts.put(ChunkEntity.IdFromNbt(nbt), nbt);
    }

    @Nullable
    public NbtCompound getChunkEntityNbt(ChunkEntityId id) {
        return this.chunkEntityNbts.get(id);
    }


    @Unique
    public abstract NbtCompound getPackedChunkEntityNbt(ChunkEntityId id);

    @Unique
    @Override
    public void addChunkEntity(ChunkEntity chunkEntity) {

    }

    @Override
    public abstract ChunkEntitiesTickSchedulers getChunkEntityTickSchedulers();

    public Set<ChunkEntityId> getChunkEntitiesId() {
        HashSet<ChunkEntityId> set = Sets.newHashSet(this.chunkEntityNbts.keySet());
        set.addAll(this.chunkEntities.keySet());
        return set;
    }
}
