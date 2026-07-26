package com.julibiddewer.beekeeper.mixin;

import com.julibiddewer.beekeeper.BeekeeperSmokeState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes a beehive / bee nest report itself as "sedated" (calm bees) while it is smoked by the
 * beekeeper smoker. Vanilla consults {@code isFireNearby()} / {@code isSedated()} both when
 * honey is harvested (shears / bottle) and when the hive is broken, skipping the bee-angering
 * logic when a campfire is nearby. By returning {@code true} here for smoked hives we get the
 * exact same effect without needing a real fire.
 */
@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveBlockEntityMixin {
    @Inject(method = "isFireNearby", at = @At("HEAD"), cancellable = true)
    private void beekeeper$isFireNearby(CallbackInfoReturnable<Boolean> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        BlockPos pos = self.getBlockPos();
        if (BeekeeperSmokeState.isSmoked(level, pos)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isSedated", at = @At("HEAD"), cancellable = true)
    private void beekeeper$isSedated(CallbackInfoReturnable<Boolean> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        BlockPos pos = self.getBlockPos();
        if (BeekeeperSmokeState.isSmoked(level, pos)) {
            cir.setReturnValue(true);
        }
    }
}