package azokh99.realfurnaces.mixin;

import azokh99.realfurnaces.RealFurnaces;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelItemMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractFireBlock;getState(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", shift = At.Shift.BEFORE) ,method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", cancellable = true,locals = LocalCapture.CAPTURE_FAILHARD)
    private void injected(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, PlayerEntity playerEntity, World world, BlockPos blockPos, BlockState blockState, BlockPos blockPos2) {

        if (context.getSide() == Direction.UP && blockState.isOf(Blocks.COAL_BLOCK)) {


            ItemStack itemStack = context.getStack();
            if (playerEntity instanceof ServerPlayerEntity) {
                BlockState blockState2 = RealFurnaces.USED_COAL_BLOCK.getDefaultState();
                world.setBlockState(blockPos, blockState2, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                BlockState blockState3 = RealFurnaces.FURNACE_FIRE_BLOCK.getDefaultState();
                world.setBlockState(blockPos2, blockState3, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);

                Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos2, itemStack);
                itemStack.damage(1, playerEntity, p -> p.sendToolBreakStatus(context.getHand()));
            } else {
                BlockState blockState2 = Blocks.COAL_BLOCK.getDefaultState();
                world.setBlockState(blockPos, blockState2, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                BlockState blockState3 = Blocks.FIRE.getDefaultState();
                world.setBlockState(blockPos2, blockState3, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);

            }
            cir.setReturnValue(ActionResult.success(world.isClient()));
            cir.cancel();
            //TODO detect furnace and spawn tileentity
        }
    }
}
