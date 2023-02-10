package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.WorldChunk;

public class DirectChunkEntityTickInvoker<T extends ChunkEntity>
        implements ChunkEntityTickInvoker {
    private final T chunkEntity;
    private final ChunkEntityTicker<T> ticker;
    //private boolean hasWarned;
    final /* synthetic */ WorldChunk worldChunk;

    public DirectChunkEntityTickInvoker(WorldChunk worldChunk, T chunkEntity, ChunkEntityTicker<T> ticker) {
        this.worldChunk = worldChunk;
        this.chunkEntity = chunkEntity;
        this.ticker = ticker;
    }

    @Override
    public void tick() {
        BlockPos blockPos;
        if (!((ChunkEntity)this.chunkEntity).isRemoved() && ((ChunkEntity)this.chunkEntity).hasWorld()) {
            try {
                Profiler profiler = this.worldChunk.getWorld().getProfiler();
                profiler.push(this::getName);
                /*if (((GlobalEntity)this.chunkEntity).getType().supports(blockState)) {
                    this.ticker.tick(this.worldChunk.getWorld(), this.chunkEntity);
                    this.hasWarned = false;
                /*} else*/ /*if (!this.hasWarned) {
                    this.hasWarned = true;
                    LogUtils.getLogger().warn("Block entity {} @ {} state >BS< invalid for ticking:", LogUtils.defer(this::getName), LogUtils.defer(this::getPos), blockState);
                } else {*/
                    this.ticker.tick(this.worldChunk.getWorld(), this.chunkEntity);
                    //this.hasWarned = false;
                //}
                profiler.pop();
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.create(throwable, "Ticking chunk entity");
                CrashReportSection crashReportSection = crashReport.addElement("Chunk entity being ticked");
                ((ChunkEntity)this.chunkEntity).populateCrashReport(crashReportSection);
                throw new CrashException(crashReport);
            }
        }
    }

    @Override
    public boolean isRemoved() {
        return ((ChunkEntity)this.chunkEntity).isRemoved();
    }

    @Override
    public ChunkPos getPos() {
        return ((ChunkEntity)this.chunkEntity).getPos();
    }

    @Override
    public String getName() {
        return "Chunk Entity"/*BlockEntityType.getId(((BlockEntity)this.chunkEntity).getType()).toString()*/;
    }

    public String toString() {
        return "Level ticker for " + this.getName() + "@" + this.getPos();
    }
}