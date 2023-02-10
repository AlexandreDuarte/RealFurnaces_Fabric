package azokh99.realfurnaces.block;

import azokh99.realfurnaces.RealFurnaces;
import azokh99.realfurnaces.chunk.entity.ChunkEntityId;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FurnaceBlockEntity extends BlockEntity {

    private static float startingInternalEnergy = 1600f;

    private float heat = 0f;
    private float internalEnergy = 1600f;
    private float marginHeat = 0f;
    private ChunkEntityId globalEntityId = new ChunkEntityId();

    public FurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(RealFurnaces.FURNACE_BLOCK_ENTITY, pos, state);
    }



    public void removeFromInternalEnergy(float delta) {
        internalEnergy -= delta;
    }

    public float getInternalEnergy() {
        return internalEnergy;
    }

    public void addToHeat(float delta) {
        this.heat += delta;
    }

    public float getHeat() {
        return heat;
    }

    public void addToMarginHeat(float delta) {
        this.marginHeat += delta;
    }

    public float getMarginHeat() {
        return marginHeat;
    }

    public static float getStartingInternalEnergy() {
        return startingInternalEnergy;
    }

    public void addToInternalEnergy(float delta) {
        this.internalEnergy += delta;
    }

    public ChunkEntityId getGlobalEntityId() {
        return globalEntityId;
    }

    public void setGlobalEntityId(int globalEntityId) {
        this.globalEntityId.setId(globalEntityId);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putFloat("heat", heat);
        nbt.putFloat("internal_energy", internalEnergy);
        nbt.putFloat("margin_heat", marginHeat);
        nbt.putInt("chunk_entity", globalEntityId.getId());

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        heat = nbt.getFloat("heat");
        internalEnergy = nbt.getFloat("internal_energy");
        marginHeat = nbt.getFloat("margin_heat");
        globalEntityId = new ChunkEntityId(nbt.getInt("chunk_entity"));
    }

}
