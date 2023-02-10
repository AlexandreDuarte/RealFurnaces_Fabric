package azokh99.realfurnaces.chunk.entity;

import azokh99.realfurnaces.world.ChunkEntityTickInvoker;
import azokh99.realfurnaces.world.ChunkEntityTicker;
import com.mojang.logging.LogUtils;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    //private final GlobalEntityType<?> type;
    protected final ChunkPos pos;
    protected ChunkEntityId id;

    @Nullable
    protected World world;

    protected boolean removed;

    public ChunkEntity(/*GlobalEntityType<?> type, */ChunkPos pos) {
        /*this.type = type;*/
        this.pos = pos;
        this.id = new ChunkEntityId();
    }

    public ChunkEntity(/*GlobalEntityType<?> type, */ChunkEntityId id, ChunkPos pos) {
        /*this.type = type;*/
        this.pos = pos;
        this.id = id;
    }

    /**
     * {@return the block position from {@code nbt}}
     *
     * <p>The passed NBT should use lowercase {@code x}, {@code y}, and {@code z}
     * keys to store the position. This is incompatible with {@link
     * net.minecraft.nbt.NbtHelper#fromBlockPos} that use uppercase keys.
     */
    public static ChunkPos posFromNbt(NbtCompound nbt) {
        return new ChunkPos(nbt.getInt("x"), nbt.getInt("z"));
    }

    public static ChunkEntityId IdFromNbt(NbtCompound nbt) {
        return new ChunkEntityId(nbt.getInt("uid"));
    }

    /**
     * {@return the world the block entity belongs to}
     *
     * <p>This can return {@code null} during world generation.
     */
    @Nullable
    public World getWorld() {
        return this.world;
    }

    /**
     * Sets the world the block entity belongs to.
     *
     * <p>This should not be called manually; however, this can be overridden
     * to initialize fields dependent on the world.
     */
    public void setWorld(World world) {
        this.world = world;
    }

    public boolean hasWorld() {
        return this.world != null;
    }

    @Nullable
    public <T extends ChunkEntity> ChunkEntityTicker<T> getTicker(World world/*, GlobalEntityType<?> globalEntityType*/) {
        return ChunkEntity::tick;
    }

    private static void tick(World world, ChunkEntity t) {

        //System.out.println("HALLO");
    }

    /**
     * Reads data from {@code nbt}. Subclasses should override this if they
     * store a persistent data.
     *
     * <p>NBT is a storage format; therefore, a data from NBT is loaded to a
     * block entity instance's fields, which are used for other operations instead
     * of the NBT. The data is written back to NBT when saving the block entity.
     *
     * <p>{@code nbt} might not have all expected keys, or might have a key whose
     * value does not meet the requirement (such as the type or the range). This
     * method should fall back to a reasonable default value instead of throwing an
     * exception.
     *
     * @see #writeNbt
     */
    public void readNbt(NbtCompound nbt) {
    }

    /**
     * Writes data to {@code nbt}. Subclasses should override this if they
     * store a persistent data.
     *
     * <p>NBT is a storage format; therefore, a data from NBT is loaded to a
     * block entity instance's fields, which are used for other operations instead
     * of the NBT. The data is written back to NBT when saving the block entity.
     *
     * @see #readNbt
     */
    protected void writeNbt(NbtCompound nbt) {
    }

    /**
     * {@return the block entity's NBT data with identifying data}
     *
     * <p>In addition to data written at {@link #writeNbt}, this also
     * writes the {@linkplain #writeIdToNbt block entity type ID} and the
     * position of the block entity.
     *
     * @see #createNbt
     * @see #createNbtWithId
     */
    public final NbtCompound createNbtWithIdentifyingData() {
        NbtCompound nbtCompound = this.createNbt();
        this.writeIdentifyingData(nbtCompound);
        return nbtCompound;
    }

    /**
     * {@return the block entity's NBT data with block entity type ID}
     *
     * <p>In addition to data written at {@link #writeNbt}, this also
     * writes the {@linkplain #writeIdToNbt block entity type ID}.
     *
     * @see #createNbt
     * @see #createNbtWithIdentifyingData
     */
    public final NbtCompound createNbtWithId() {
        NbtCompound nbtCompound = this.createNbt();
        this.writeIdToNbt(nbtCompound);
        return nbtCompound;
    }

    /**
     * {@return the block entity's NBT data}
     *
     * <p>Internally, this calls {@link #writeNbt} with a new {@link NbtCompound}
     * and returns the compound.
     *
     * @see #writeNbt
     * @see #createNbtWithIdentifyingData
     * @see #createNbtWithId
     */
    public final NbtCompound createNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        this.writeNbt(nbtCompound);
        return nbtCompound;
    }

    /**
     * Writes the block entity type ID to {@code nbt} under the {@code id} key.
     *
     * @throws RuntimeException if the block entity type is not registered in
     * the registry
     */
    private void writeIdToNbt(NbtCompound nbt) {
        /*Identifier identifier = GlobalEntityType.getId(this.getType());
        if (identifier == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        nbt.putString("id", identifier.toString());*/
        nbt.putInt("uid", getId().getId());
        nbt.putLong("pos", getPos().toLong());
    }

    /*public GlobalEntityType<?> getType() {
        return type;
    }*/

    /**
     * Writes the ID of {@code type} to {@code nbt} under the {@code id} key.
     */
    /*public static void writeIdToNbt(NbtCompound nbt, GlobalEntityType<?> type) {
        nbt.putString("id", GlobalEntityType.getId(type).toString());
    }*/

    /**
     * Sets {@code stack}'s {@code net.minecraft.item.BlockItem#BLOCK_ENTITY_TAG_KEY}
     * NBT value to {@linkplain #createNbt the block entity's NBT data}.
     */
    /*public void setStackNbt(ItemStack stack) {
        BlockItem.setBlockEntityNbt(stack, this.getType(), this.createNbt());
    }*/

    /**
     * Writes to {@code nbt} the block entity type ID under the {@code id} key,
     * and the block's position under {@code x}, {@code y}, and {@code z} keys.
     *
     * @throws RuntimeException if the block entity type is not registered in
     * the registry
     */
    private void writeIdentifyingData(NbtCompound nbt) {
        this.writeIdToNbt(nbt);
    }

    /**
     * {@return the new block entity loaded from {@code nbt}, or {@code null} if it fails}
     *
     * <p>This is used during chunk loading. This can fail if {@code nbt} has an improper or
     * unregistered {@code id}, or if {@link #readNbt} throws an exception; in these cases,
     * this logs an error and returns {@code null}.
     */
    @Nullable
    public static ChunkEntity createFromNbt(NbtCompound nbt) {
        String string = nbt.getString("id");
        ChunkPos pos = new ChunkPos(nbt.getLong("pos"));
        ChunkEntityId id = new ChunkEntityId(nbt.getInt("uid"));
        return new ChunkEntity(id, pos);
        /*Identifier identifier = Identifier.tryParse(string);
        if (identifier == null) {
            LOGGER.error("Block entity has invalid type: {}", (Object)string);
            return null;
        }*/
        /*//TODO Registry for GlobalEntityType?
        return Registries.BLOCK_ENTITY_TYPE.getOrEmpty(identifier).map(type -> {
            try {
                return type.instantiate(pos, state);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to create global entity {}", (Object)string, (Object)throwable);
                return null;
            }
        }).map(globalEntity -> {
            try {
                globalEntity.readNbt(nbt);
                return globalEntity;
            } catch (Throwable throwable) {
                LOGGER.error("Failed to load data for global entity {}", (Object)string, (Object)throwable);
                return null;
            }
        }).orElseGet(() -> {
            LOGGER.warn("Skipping GlobalEntity with id {}", (Object)string);
            return null;
        });*/
    }

    /**
     * Marks this block entity as dirty and that it needs to be saved.
     * This also triggers {@linkplain World#updateComparators comparator update}.
     *
     * <p>This <strong>must be called</strong> when something changed in a way that
     * affects the saved NBT; otherwise, the game might not save the block entity.
     */
    public void markDirty() {
        if (this.world != null) {
            ChunkEntity.markDirty(this.world, this.pos);
        }
    }

    protected static void markDirty(World world, ChunkPos pos) {
        world.getChunk(pos.x, pos.z).setNeedsSaving(true);
    }



    /**
     * {@return the packet to send to nearby players when the block entity's observable
     * state changes, or {@code null} to not send the packet}
     *
     * <p>If the data returned by {@link #toInitialChunkDataNbt initial chunk data} is suitable
     * for updates, the following shortcut can be used to create an update packet: {@code
     * BlockEntityUpdateS2CPacket.create(this)}. The NBT will be passed to {@link #readNbt}
     * on the client.
     *
     * <p>"Observable state" is a state that clients can observe without specific interaction.
     * For example, {@link CampfireBlockEntity}'s cooked items are observable states,
     * but chests' inventories are not observable states, since the player must first open
     * that chest before they can see the contents.
     *
     * <p>To sync block entity data using this method, use {@code
     * serverWorld.getChunkManager().markForUpdate(this.getPos());}.
     *
     * @see #toInitialChunkDataNbt
     */
    @Nullable
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return null;
    }

    /**
     * {@return the serialized state of this block entity that is observable by clients}
     *
     * <p>This is sent alongside the initial chunk data, as well as when the block
     * entity implements {@link #toUpdatePacket} and decides to use the default
     * {@link net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket}.
     *
     * <p>"Observable state" is a state that clients can observe without specific interaction.
     * For example, {@link CampfireBlockEntity}'s cooked items are observable states,
     * but chests' inventories are not observable states, since the player must first open
     * that chest before they can see the contents.
     *
     * <p>To send all NBT data of this block entity saved to disk, return {@link #createNbt}.
     *
     * @see #toUpdatePacket
     */
    public NbtCompound toInitialChunkDataNbt() {
        return new NbtCompound();
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public void markRemoved() {
        this.removed = true;
    }

    public void cancelRemoval() {
        this.removed = false;
    }

    /**
     * If this block entity's block extends {@link net.minecraft.block.BlockWithEntity},
     * this is called inside {@link net.minecraft.block.AbstractBlock#onSyncedBlockEvent}.
     *
     * @see net.minecraft.block.AbstractBlock#onSyncedBlockEvent
     */
    public boolean onSyncedBlockEvent(int type, int data) {
        return false;
    }

    public void populateCrashReport(CrashReportSection crashReportSection) {
        //TODO Registries and Stuff
        //crashReportSection.add("Name", () -> Registries.BLOCK_ENTITY_TYPE.getId(this.getType()) + " // " + this.getClass().getCanonicalName());
        if (this.world == null) {
            return;
        }
        //CrashReportSection.addBlockInfo(crashReportSection, this.world, this.pos, this.getCachedState());
        //CrashReportSection.addBlockInfo(crashReportSection, this.world, this.pos, this.world.getBlockState(this.pos));
    }

    /**
     * {@return whether the block item should require the player to have operator
     * permissions to copy the block entity data on placement}
     *
     * <p>Block entities that can execute commands should override this to return
     * {@code true}.
     *
     * @see net.minecraft.entity.player.PlayerEntity#isCreativeLevelTwoOp
     */
    public boolean copyItemDataRequiresOperator() {
        return false;
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public void scheduledTick(ServerWorld serverWorld, ChunkEntityId id, Random random) {
        System.out.println("SCHEDULED HALLO");
    }

    public ChunkEntityId getId() {
        return this.id;
    }

    public void setWrapped(ChunkEntityTickInvoker emptyGlobalEntityTicker) {
    }

    public void setId(ChunkEntityId globalEntityId) {
        this.id = globalEntityId;
    }

    /*public BlockEntityType<?> getType() {
        return this.type;
    }*/

    /*@Deprecated
    public void setCachedState(BlockState state) {
        this.cachedState = state;
    }*/
}
