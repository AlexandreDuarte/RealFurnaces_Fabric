package azokh99.realfurnaces.world;


import azokh99.realfurnaces.chunk.entity.ChunkEntity;
import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record ChunkTick<T>(T type, ChunkEntityId id, ChunkPos pos, int delay, TickPriority priority) {
    private static final String TYPE_NBT_KEY = "i";
    private static final String X_NBT_KEY = "x";
    private static final String Y_NBT_KEY = "y";
    private static final String Z_NBT_KEY = "z";
    private static final String ID_NBT_KEY = "d";
    private static final String DELAY_NBT_KEY = "t";
    private static final String PRIORITY_NBT_KEY = "p";
    public static final Hash.Strategy<ChunkTick<?>> HASH_STRATEGY = new Hash.Strategy<ChunkTick<?>>() {
        public int hashCode(ChunkTick<?> tick) {
            return 31 * tick.id().getId() + tick.type().hashCode();
        }

        public boolean equals(@Nullable ChunkTick<?> tick, @Nullable ChunkTick<?> tick2) {
            if (tick == tick2) {
                return true;
            } else if (tick != null && tick2 != null) {
                return tick.type() == tick2.type() && tick.id() == tick2.id() && tick.pos() == tick2.pos();
            } else {
                return false;
            }
        }
    };

    public ChunkTick(T type, ChunkEntityId id, ChunkPos pos, int delay, TickPriority priority) {
        this.type = type;
        this.id = id;
        this.pos = pos;
        this.delay = delay;
        this.priority = priority;
    }

    public static <T> void tick(NbtList tickList, Function<String, Optional<T>> nameToTypeFunction, ChunkPos pos, Consumer<ChunkTick<T>> tickConsumer) {
        long l = pos.toLong();

        for(int i = 0; i < tickList.size(); ++i) {
            NbtCompound nbtCompound = tickList.getCompound(i);
            fromNbt(nbtCompound, nameToTypeFunction).ifPresent((tick) -> {
                if (tick.pos().toLong() == l) {
                    tickConsumer.accept(tick);
                }

            });
        }

    }

    public static <T> Optional<ChunkTick<T>> fromNbt(NbtCompound nbt, Function<String, Optional<T>> nameToType) {
            ChunkPos pos = new ChunkPos(nbt.getInt("x"), nbt.getInt("z"));
            ChunkEntity ge = new ChunkEntity(pos);
            ge.setId(new ChunkEntityId(nbt.getInt("id")));
            return Optional.of(new ChunkTick(ge, ge.getId(), pos,  nbt.getInt("t"), TickPriority.byIndex(nbt.getInt("p"))));
    }

    private static NbtCompound toNbt(String type, ChunkEntityId id, ChunkPos pos, int delay, TickPriority priority) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("i", type);
        nbtCompound.putInt("id", id.getId());
        nbtCompound.putInt("x", pos.getCenterX());
        nbtCompound.putInt("z", pos.getCenterZ());
        nbtCompound.putInt("t", delay);
        nbtCompound.putInt("p", priority.getIndex());
        return nbtCompound;
    }

    public static <T> NbtCompound orderedTickToNbt(IdOrderedTick<T> orderedTick, Function<T, String> typeToNameFunction, long delay) {
        return toNbt((String)typeToNameFunction.apply(orderedTick.type()), orderedTick.id(), orderedTick.pos(), (int)(orderedTick.triggerTick() - delay), orderedTick.priority());
    }

    public NbtCompound toNbt(Function<T, String> typeToNameFunction) {
        return toNbt((String)typeToNameFunction.apply(this.type), this.id, this.pos, this.delay, this.priority);
    }

    public IdOrderedTick<T> createOrderedTick(long time, long subTickOrder) {
        return new IdOrderedTick<T>(this.type, this.id, this.pos, time + (long)this.delay, this.priority, subTickOrder);
    }

    public static <T> ChunkTick<T> create(T type, ChunkEntityId id, ChunkPos pos) {
        return new ChunkTick<T>(type, id, pos,0, TickPriority.NORMAL);
    }

    public T type() {
        return this.type;
    }

    public ChunkEntityId id() {return this.id;}

    public ChunkPos pos() {
        return this.pos;
    }

    public int delay() {
        return this.delay;
    }

    public TickPriority priority() {
        return this.priority;
    }
}
