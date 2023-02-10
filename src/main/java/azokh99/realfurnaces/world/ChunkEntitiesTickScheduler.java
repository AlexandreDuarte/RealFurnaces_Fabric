package azokh99.realfurnaces.world;


import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.tick.Tick;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ChunkEntitiesTickScheduler<T>
        implements ChunkEntitiesSerializableTickScheduler<T>,
        BasicChunkTickScheduler<T> {
    private final Queue<IdOrderedTick<T>> tickQueue = new PriorityQueue(IdOrderedTick.TRIGGER_TICK_COMPARATOR);
    @Nullable
    private List<ChunkTick<T>> ticks;
    private final Set<IdOrderedTick<?>> queuedTicks = new ObjectOpenCustomHashSet(IdOrderedTick.HASH_STRATEGY);
    @Nullable
    private BiConsumer<ChunkEntitiesTickScheduler<T>, IdOrderedTick<T>> tickConsumer;

    public ChunkEntitiesTickScheduler() {
    }

    public ChunkEntitiesTickScheduler(List<ChunkTick<T>> ticks) {
        this.ticks = ticks;
        for (ChunkTick<T> tick : ticks) {
            this.queuedTicks.add(IdOrderedTick.create(tick.type(), tick.pos(), tick.id()));
        }
    }

    public void setTickConsumer(@Nullable BiConsumer<ChunkEntitiesTickScheduler<T>, IdOrderedTick<T>> tickConsumer) {
        this.tickConsumer = tickConsumer;
    }

    @Nullable
    public IdOrderedTick<T> peekNextTick() {
        return this.tickQueue.peek();
    }

    @Nullable
    public IdOrderedTick<T> pollNextTick() {
        IdOrderedTick<T> orderedTick = this.tickQueue.poll();
        if (orderedTick != null) {
            this.queuedTicks.remove(orderedTick);
        }
        return orderedTick;
    }

    @Override
    public void scheduleTick(IdOrderedTick<T> orderedTick) {
        if (this.queuedTicks.add(orderedTick)) {
            this.queueTick(orderedTick);
        }
    }

    private void queueTick(IdOrderedTick<T> orderedTick) {
        this.tickQueue.add(orderedTick);
        if (this.tickConsumer != null) {
            this.tickConsumer.accept(this, orderedTick);
        }
    }

    @Override
    public boolean isQueued(T type, ChunkEntityId id, ChunkPos pos) {
        return this.queuedTicks.contains(IdOrderedTick.create(type, pos, id));
    }

    public void removeTicksIf(Predicate<IdOrderedTick<T>> predicate) {
        Iterator iterator = this.tickQueue.iterator();
        while (iterator.hasNext()) {
            IdOrderedTick orderedTick = (IdOrderedTick)iterator.next();
            if (!predicate.test(orderedTick)) continue;
            iterator.remove();
            this.queuedTicks.remove(orderedTick);
        }
    }

    public Stream<IdOrderedTick<T>> getQueueAsStream() {
        return this.tickQueue.stream();
    }

    @Override
    public int getTickCount() {
        return this.tickQueue.size() + (this.ticks != null ? this.ticks.size() : 0);
    }

    @Override
    public NbtList toNbt(long l, Function<T, String> function) {
        NbtList nbtList = new NbtList();
        if (this.ticks != null) {
            for (ChunkTick<T> tick : this.ticks) {
                nbtList.add(tick.toNbt(function));
            }
        }
        for (IdOrderedTick orderedTick : this.tickQueue) {
            nbtList.add(ChunkTick.orderedTickToNbt(orderedTick, function, l));
        }
        return nbtList;
    }

    public void disable(long time) {
        if (this.ticks != null) {
            int i = -this.ticks.size();
            for (ChunkTick<T> tick : this.ticks) {
                this.queueTick(tick.createOrderedTick(time, i++));
            }
        }
        this.ticks = null;
    }

    public static <T> ChunkEntitiesTickScheduler<T> create(NbtList tickQueue, Function<String, Optional<T>> nameToTypeFunction, ChunkPos pos) {
        ImmutableList.Builder builder = ImmutableList.builder();
        Tick.tick(tickQueue, nameToTypeFunction, pos, builder::add);
        return new ChunkEntitiesTickScheduler<T>(builder.build());
    }
}