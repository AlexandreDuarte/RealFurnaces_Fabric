package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import net.minecraft.world.World;

@FunctionalInterface
public interface ChunkEntityTicker<T extends ChunkEntity> {
    /**
     * Ticks the block entity.
     */
    public void tick(World var1, T var4);
}

