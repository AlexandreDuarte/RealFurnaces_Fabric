package azokh99.realfurnaces.world;

import net.minecraft.util.math.ChunkPos;

public interface ChunkEntityTickInvoker {
    public void tick();

    public boolean isRemoved();

    public ChunkPos getPos();

    public String getName();
}

