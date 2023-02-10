package azokh99.realfurnaces.world;

import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Supplier;

public class WorldChunkEntitiesTickScheduler<T>
        implements QueryableChunkEntitiesTickScheduler<T> {
    private static final Comparator<ChunkEntitiesTickScheduler<?>> COMPARATOR = (a, b) -> IdOrderedTick.BASIC_COMPARATOR.compare(a.peekNextTick(), b.peekNextTick());
    private final LongPredicate tickingFutureReadyPredicate;
    private final Supplier<Profiler> profilerGetter;
    private final Long2ObjectMap<ChunkEntitiesTickScheduler<T>> chunkTickSchedulers = new Long2ObjectOpenHashMap<ChunkEntitiesTickScheduler<T>>();
    private final Long2LongMap nextTriggerTickByChunkPos = Util.make(new Long2LongOpenHashMap(), map -> map.defaultReturnValue(Long.MAX_VALUE));
    private final Queue<ChunkEntitiesTickScheduler<T>> tickableChunkTickSchedulers = new PriorityQueue(COMPARATOR);
    private final Queue<IdOrderedTick<T>> tickableTicks = new ArrayDeque<IdOrderedTick<T>>();
    private final List<IdOrderedTick<T>> tickedTicks = new ArrayList<IdOrderedTick<T>>();
    private final Set<IdOrderedTick<?>> copiedTickableTicksList = new ObjectOpenCustomHashSet(IdOrderedTick.HASH_STRATEGY);
    private final BiConsumer<ChunkEntitiesTickScheduler<T>, IdOrderedTick<T>> queuedTickConsumer = (chunkTickScheduler, tick) -> {
        if (tick.equals(chunkTickScheduler.peekNextTick())) {
            this.schedule((IdOrderedTick<T>)tick);
        }
    };

    public WorldChunkEntitiesTickScheduler(LongPredicate tickingFutureReadyPredicate, Supplier<Profiler> profilerGetter) {
        this.tickingFutureReadyPredicate = tickingFutureReadyPredicate;
        this.profilerGetter = profilerGetter;
    }

    public void addChunkTickScheduler(ChunkPos pos, ChunkEntitiesTickScheduler<T> scheduler) {
        long l = pos.toLong();
        this.chunkTickSchedulers.put(l, scheduler);
        IdOrderedTick<T> orderedTick = scheduler.peekNextTick();
        if (orderedTick != null) {
            this.nextTriggerTickByChunkPos.put(l, orderedTick.triggerTick());
        }
        scheduler.setTickConsumer(this.queuedTickConsumer);
    }

    public void removeChunkTickScheduler(ChunkPos pos) {
        long l = pos.toLong();
        ChunkEntitiesTickScheduler chunkTickScheduler = (ChunkEntitiesTickScheduler)this.chunkTickSchedulers.remove(l);
        this.nextTriggerTickByChunkPos.remove(l);
        if (chunkTickScheduler != null) {
            chunkTickScheduler.setTickConsumer(null);
        }
    }

    @Override
    public void scheduleTick(IdOrderedTick<T> orderedTick) {
        long l = orderedTick.id().getId();
        ChunkEntitiesTickScheduler chunkTickScheduler = (ChunkEntitiesTickScheduler)this.chunkTickSchedulers.get(l);
        if (chunkTickScheduler == null) {
            Util.throwOrPause(new IllegalStateException("Trying to schedule tick in not loaded position" + orderedTick.id()));
            return;
        }
        chunkTickScheduler.scheduleTick(orderedTick);
    }

    public void tick(long time, int maxTicks, BiConsumer<ChunkEntityId, T> ticker) {
        Profiler profiler = this.profilerGetter.get();
        profiler.push("collect");
        this.collectTickableTicks(time, maxTicks, profiler);
        profiler.swap("run");
        profiler.visit("ticksToRun", this.tickableTicks.size());
        this.tick(ticker);
        profiler.swap("cleanup");
        this.clear();
        profiler.pop();
    }

    private void collectTickableTicks(long time, int maxTicks, Profiler profiler) {
        this.collectTickableChunkTickSchedulers(time);
        profiler.visit("containersToTick", this.tickableChunkTickSchedulers.size());
        this.addTickableTicks(time, maxTicks);
        this.delayAllTicks();
    }

    private void collectTickableChunkTickSchedulers(long time) {
        ObjectIterator<Long2LongMap.Entry> objectIterator = Long2LongMaps.fastIterator(this.nextTriggerTickByChunkPos);
        while (objectIterator.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry)objectIterator.next();
            long l = entry.getLongKey();
            long m = entry.getLongValue();
            if (m > time) continue;
            ChunkEntitiesTickScheduler chunkTickScheduler = (ChunkEntitiesTickScheduler)this.chunkTickSchedulers.get(l);
            if (chunkTickScheduler == null) {
                objectIterator.remove();
                continue;
            }
            IdOrderedTick orderedTick = chunkTickScheduler.peekNextTick();
            if (orderedTick == null) {
                objectIterator.remove();
                continue;
            }
            if (orderedTick.triggerTick() > time) {
                entry.setValue(orderedTick.triggerTick());
                continue;
            }
            if (!this.tickingFutureReadyPredicate.test(l)) continue;
            objectIterator.remove();
            this.tickableChunkTickSchedulers.add(chunkTickScheduler);
        }
    }

    private void addTickableTicks(long time, int maxTicks) {
        ChunkEntitiesTickScheduler<T> chunkTickScheduler;
        while (this.isTickableTicksCountUnder(maxTicks) && (chunkTickScheduler = this.tickableChunkTickSchedulers.poll()) != null) {
            IdOrderedTick<T> orderedTick = chunkTickScheduler.pollNextTick();
            this.addTickableTick(orderedTick);
            this.addTickableTicks(this.tickableChunkTickSchedulers, chunkTickScheduler, time, maxTicks);
            IdOrderedTick<T> orderedTick2 = chunkTickScheduler.peekNextTick();
            if (orderedTick2 == null) continue;
            if (orderedTick2.triggerTick() <= time && this.isTickableTicksCountUnder(maxTicks)) {
                this.tickableChunkTickSchedulers.add(chunkTickScheduler);
                continue;
            }
            this.schedule(orderedTick2);
        }
    }

    private void delayAllTicks() {
        for (ChunkEntitiesTickScheduler chunkTickScheduler : this.tickableChunkTickSchedulers) {
            this.schedule(chunkTickScheduler.peekNextTick());
        }
    }

    private void schedule(IdOrderedTick<T> tick) {
        this.nextTriggerTickByChunkPos.put(tick.id().getId(), tick.triggerTick());
    }

    private void addTickableTicks(Queue<ChunkEntitiesTickScheduler<T>> tickableChunkTickSchedulers, ChunkEntitiesTickScheduler<T> chunkTickScheduler, long tick, int maxTicks) {
        IdOrderedTick<T> orderedTick2;
        IdOrderedTick<T> orderedTick;
        if (!this.isTickableTicksCountUnder(maxTicks)) {
            return;
        }
        ChunkEntitiesTickScheduler<T> chunkTickScheduler2 = tickableChunkTickSchedulers.peek();
        IdOrderedTick<T> orderedTick3 = orderedTick = chunkTickScheduler2 != null ? chunkTickScheduler2.peekNextTick() : null;
        while (this.isTickableTicksCountUnder(maxTicks) && (orderedTick2 = chunkTickScheduler.peekNextTick()) != null && orderedTick2.triggerTick() <= tick && (orderedTick == null || IdOrderedTick.BASIC_COMPARATOR.compare(orderedTick2, orderedTick) <= 0)) {
            chunkTickScheduler.pollNextTick();
            this.addTickableTick(orderedTick2);
        }
    }

    private void addTickableTick(IdOrderedTick<T> tick) {
        this.tickableTicks.add(tick);
    }

    private boolean isTickableTicksCountUnder(int maxTicks) {
        return this.tickableTicks.size() < maxTicks;
    }

    private void tick(BiConsumer<ChunkEntityId, T> ticker) {
        while (!this.tickableTicks.isEmpty()) {
            IdOrderedTick<T> orderedTick = this.tickableTicks.poll();
            if (!this.copiedTickableTicksList.isEmpty()) {
                this.copiedTickableTicksList.remove(orderedTick);
            }
            this.tickedTicks.add(orderedTick);
            ticker.accept(orderedTick.id(), orderedTick.type());
        }
    }

    private void clear() {
        this.tickableTicks.clear();
        this.tickableChunkTickSchedulers.clear();
        this.tickedTicks.clear();
        this.copiedTickableTicksList.clear();
    }

    @Override
    public boolean isQueued(T type, ChunkEntityId id, ChunkPos pos) {
        long l = id.getId();
        ChunkEntitiesTickScheduler chunkTickScheduler = (ChunkEntitiesTickScheduler)this.chunkTickSchedulers.get(l);
        return chunkTickScheduler != null && chunkTickScheduler.isQueued(type, id, pos);
    }

    @Override
    public boolean isTicking(T type, ChunkEntityId id, ChunkPos pos) {
        this.copyTickableTicksList();
        return this.copiedTickableTicksList.contains(IdOrderedTick.create(type, pos, id));
    }

    private void copyTickableTicksList() {
        if (this.copiedTickableTicksList.isEmpty() && !this.tickableTicks.isEmpty()) {
            this.copiedTickableTicksList.addAll(this.tickableTicks);
        }
    }

    private void visitChunks(BlockBox box, WorldChunkEntitiesTickScheduler.ChunkVisitor<T> visitor) {
        int i = ChunkSectionPos.getSectionCoord((double)box.getMinX());
        int j = ChunkSectionPos.getSectionCoord((double)box.getMinZ());
        int k = ChunkSectionPos.getSectionCoord((double)box.getMaxX());
        int l = ChunkSectionPos.getSectionCoord((double)box.getMaxZ());
        for (int m = i; m <= k; ++m) {
            for (int n = j; n <= l; ++n) {
                long o = ChunkPos.toLong(m, n);
                ChunkEntitiesTickScheduler chunkTickScheduler = (ChunkEntitiesTickScheduler)this.chunkTickSchedulers.get(o);
                if (chunkTickScheduler == null) continue;
                visitor.accept(o, chunkTickScheduler);
            }
        }
    }

    /*public void clearNextTicks(BlockBox box) {
        Predicate<IdOrderedTick> predicate = tick -> box.contains(tick.pos());
        this.visitChunks(box, (chunkPos, chunkTickScheduler) -> {
            IdOrderedTick orderedTick = chunkTickScheduler.peekNextTick();
            chunkTickScheduler.removeTicksIf(predicate);
            IdOrderedTick orderedTick2 = chunkTickScheduler.peekNextTick();
            if (orderedTick2 != orderedTick) {
                if (orderedTick2 != null) {
                    this.schedule(orderedTick2);
                } else {
                    this.nextTriggerTickByChunkPos.remove(chunkPos);
                }
            }
        });
        this.tickedTicks.removeIf(predicate);
        this.tickableTicks.removeIf(predicate);
    }*/

    /*public void scheduleTicks(BlockBox box, Vec3i offset) {
        ArrayList list = new ArrayList();
        Predicate<IdOrderedTick> predicate = tick -> box.contains(tick.pos());
        this.tickedTicks.stream().filter(predicate).forEach(list::add);
        this.tickableTicks.stream().filter(predicate).forEach(list::add);
        this.visitChunks(box, (chunkPos, chunkTickScheduler) -> chunkTickScheduler.getQueueAsStream().filter(predicate).forEach(list::add));
        LongSummaryStatistics longSummaryStatistics = list.stream().mapToLong(IdOrderedTick::subTickOrder).summaryStatistics();
        long l = longSummaryStatistics.getMin();
        long m = longSummaryStatistics.getMax();
        list.forEach(tick -> this.scheduleTick(new IdOrderedTick(tick.type(), tick.pos().add(offset), tick.triggerTick(), tick.priority(), tick.subTickOrder() - l + m + 1L)));
    }*/

    @Override
    public int getTickCount() {
        return this.chunkTickSchedulers.values().stream().mapToInt(IChunkEntitiesTickScheduler::getTickCount).sum();
    }

    @FunctionalInterface
    static interface ChunkVisitor<T> {
        public void accept(long var1, ChunkEntitiesTickScheduler<T> var3);
    }
}