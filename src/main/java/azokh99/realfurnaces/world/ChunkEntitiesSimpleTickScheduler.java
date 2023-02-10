package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ChunkEntitiesSimpleTickScheduler<T>
        implements ChunkEntitiesSerializableTickScheduler<T>,
        BasicChunkTickScheduler<T> {
    private final List<ChunkTick<T>> scheduledTicks = Lists.newArrayList();
    private final Set<ChunkTick<?>> scheduledTicksSet = new ObjectOpenCustomHashSet(ChunkTick.HASH_STRATEGY);

    @Override
    public void scheduleTick(IdOrderedTick<T> orderedTick) {
        ChunkTick<T> tick = new ChunkTick<T>(orderedTick.type(), orderedTick.id(), orderedTick.pos(), 0, orderedTick.priority());
        this.scheduleTick(tick);
    }

    private void scheduleTick(ChunkTick<T> tick) {
        if (this.scheduledTicksSet.add(tick)) {
            this.scheduledTicks.add(tick);
        }
    }

    @Override
    public boolean isQueued(T type, ChunkEntityId id, ChunkPos pos) {
        return this.scheduledTicksSet.contains(ChunkTick.create(type, id, pos));
    }

    @Override
    public int getTickCount() {
        return this.scheduledTicks.size();
    }

    @Override
    public NbtElement toNbt(long time, Function<T, String> typeToNameFunction) {
        NbtList nbtList = new NbtList();
        for (ChunkTick<T> tick : this.scheduledTicks) {
            nbtList.add(tick.toNbt(typeToNameFunction));
        }
        return nbtList;
    }

    public List<ChunkTick<T>> getTicks() {
        return List.copyOf(this.scheduledTicks);
    }

    public static <T> ChunkEntitiesSimpleTickScheduler<T> tick(NbtList tickList, Function<String, Optional<T>> typeToNameFunction, ChunkPos pos) {
        ChunkEntitiesSimpleTickScheduler<T> simpleTickScheduler = new ChunkEntitiesSimpleTickScheduler<T>();
        ChunkTick.tick(tickList, typeToNameFunction, pos, simpleTickScheduler::scheduleTick);
        return simpleTickScheduler;
    }
}

