package azokh99.realfurnaces.world;


import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public record IdOrderedTick<T>(T type, ChunkEntityId id, ChunkPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
    public static final Comparator<IdOrderedTick<?>> TRIGGER_TICK_COMPARATOR = (first, second) -> {
        int i = Long.compare(first.triggerTick, second.triggerTick);
        if (i != 0) {
            return i;
        } else {
            i = first.priority.compareTo(second.priority);
            return i != 0 ? i : Long.compare(first.subTickOrder, second.subTickOrder);
        }
    };
    public static final Comparator<IdOrderedTick<?>> BASIC_COMPARATOR = (first, second) -> {
        int i = first.priority.compareTo(second.priority);
        return i != 0 ? i : Long.compare(first.subTickOrder, second.subTickOrder);
    };
    public static final Hash.Strategy<IdOrderedTick<?>> HASH_STRATEGY = new Hash.Strategy<IdOrderedTick<?>>() {
        public int hashCode(IdOrderedTick<?> orderedTick) {
            return orderedTick.id().getId();
        }


        public boolean equals(@Nullable IdOrderedTick<?> orderedTick, @Nullable IdOrderedTick<?> orderedTick2) {
            if (orderedTick == orderedTick2) {
                return true;
            } else if (orderedTick != null && orderedTick2 != null) {
                return orderedTick.type() == orderedTick2.type();
            } else {
                return false;
            }
        }
    };

    public IdOrderedTick(T type, ChunkEntityId id, ChunkPos pos, long triggerTick, long subTickOrder) {
        this(type, id, pos, triggerTick, TickPriority.NORMAL, subTickOrder);
    }

    public IdOrderedTick(T type, ChunkEntityId id, ChunkPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
        this.type = type;
        this.id = id;
        this.pos = pos;
        this.triggerTick = triggerTick;
        this.priority = priority;
        this.subTickOrder = subTickOrder;
    }

    public static <T> IdOrderedTick<T> create(T type, ChunkPos pos, ChunkEntityId id) {
        return new IdOrderedTick(type, id,pos, 0L, TickPriority.NORMAL, 0L);
    }

    public T type() {
        return this.type;
    }

    @Override
    public ChunkPos pos() {
        return pos;
    }

    public long triggerTick() {
        return this.triggerTick;
    }

    public TickPriority priority() {
        return this.priority;
    }

    public long subTickOrder() {
        return this.subTickOrder;
    }
}
