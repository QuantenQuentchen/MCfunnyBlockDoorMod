package funnyblockdoormod.funnyblockdoormod.mixin;

import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IConnectable;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Inject(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void connectsTo(BlockState state, @Nullable Direction dir, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof IConnectable connectable && dir != null) {
            if (connectable.canConnect(dir, state)) {
                cir.setReturnValue(true);
                cir.cancel();
            } else {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}