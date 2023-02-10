package azokh99.realfurnaces.world;

import net.minecraft.nbt.NbtElement;

import java.util.function.Function;

public interface ChunkEntitiesSerializableTickScheduler<T> {

    public NbtElement toNbt(long l, Function<T, String> function);

}
