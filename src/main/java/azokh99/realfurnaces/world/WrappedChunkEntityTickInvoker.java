package azokh99.realfurnaces.world;

import net.minecraft.util.math.ChunkPos;

public class WrappedChunkEntityTickInvoker
        implements ChunkEntityTickInvoker {
    private ChunkEntityTickInvoker wrapped;

    public WrappedChunkEntityTickInvoker(ChunkEntityTickInvoker wrapped) {
        this.wrapped = wrapped;
    }

    public void setWrapped(ChunkEntityTickInvoker wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void tick() {
        this.wrapped.tick();
    }

    @Override
    public boolean isRemoved() {
        return this.wrapped.isRemoved();
    }

    @Override
    public ChunkPos getPos() {
        return this.wrapped.getPos();
    }

    @Override
    public String getName() {
        return this.wrapped.getName();
    }

    public String toString() {
        return this.wrapped.toString() + " <wrapped>";
    }
}
