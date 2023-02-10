package azokh99.realfurnaces;

import azokh99.realfurnaces.block.FurnaceBlockEntity;
import azokh99.realfurnaces.block.FurnaceFireBlock;
import azokh99.realfurnaces.block.UsedCoalBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RealFurnaces implements ModInitializer {

    public static final Block USED_COAL_BLOCK = new UsedCoalBlock(AbstractBlock.Settings.of(Material.STONE).luminance((state) -> 15).strength(4.0f, 5.0f));
    public static final BlockItem USED_COAL_BLOCK_ITEM = new PolymerBlockItem(USED_COAL_BLOCK, new FabricItemSettings(), Items.COAL_BLOCK);

    public static final Block FURNACE_FIRE_BLOCK = new FurnaceFireBlock(AbstractBlock.Settings.of(Material.FIRE));
    public static final BlockItem FURNACE_FIRE_BLOCK_ITEM = new PolymerBlockItem(FURNACE_FIRE_BLOCK, new FabricItemSettings(), Items.FIRE_CHARGE);

    public static final BlockEntityType<FurnaceBlockEntity> FURNACE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("rf", "furnace_block_entity"), FabricBlockEntityTypeBuilder.create(FurnaceBlockEntity::new, FURNACE_FIRE_BLOCK).build());

    @Override
    public void onInitialize() {
        PolymerBlockUtils.registerBlockEntity(FURNACE_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK, new Identifier("rf", "used_coal_block"), USED_COAL_BLOCK);
        Registry.register(Registries.ITEM, new Identifier("rf", "used_coal_block"), USED_COAL_BLOCK_ITEM);

        Registry.register(Registries.BLOCK, new Identifier("rf", "furnace_fire"), FURNACE_FIRE_BLOCK);
        Registry.register(Registries.ITEM, new Identifier("rf", "furnace_fire"), FURNACE_FIRE_BLOCK_ITEM);

    }
}
