package com.julibiddewer.beekeeper.mixin;

import com.julibiddewer.beekeeper.BeekeeperSmokeState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the "anger every bee within 8 blocks" behaviour when a smoked hive is destroyed.
 * <p>
 * Unlike honey harvesting (which is gated on {@code CampfireBlock.isSmokeyPos} and therefore already calm
 * via {@link CampfireBlockMixin}), breaking a hive calls {@code BeehiveBlock.angerNearbyBees} in
 * {@code playerDestroy} <em>unconditionally</em> (a real campfire does not protect here). Cancelling the
 * call for smoked hives ensures foreign bees in the area do not become aggressive either.
 */
@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin {
    @Inject(method = "angerNearbyBees", at = @At("HEAD"), cancellable = true)
    private void beekeeper$cancelAngerNearbyBees(Level level, BlockPos pos, CallbackInfo ci) {
        if (BeekeeperSmokeState.isSmoked(level, pos)) {
            ci.cancel();
        }
    }
}