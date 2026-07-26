package com.julibiddewer.beekeeper.mixin;

import com.julibiddewer.beekeeper.BeekeeperSmokeState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the smoker stand in for a real campfire. Vanilla's beehive harvest ({@code BeehiveBlock.useItemOn})
 * gates the bee-angering branch on {@code CampfireBlock.isSmokeyPos(level, pos)}, and
 * {@link net.minecraft.world.level.block.entity.BeehiveBlockEntity#isSedated()} simply delegates to this
 * same method. By returning {@code true} for hives that are currently smoked we therefore:
 * <ul>
 *   <li>make the harvest take the "smokey" branch (no {@code angerNearbyBees}, no emergency bee release,
 *       bees stay inside calm) - exactly like a campfire underneath, and</li>
 *   <li>make {@code isSedated()} report {@code true}, so any bees that are released (e.g. when the hive is
 *       broken) come out calm instead of attacking the player.</li>
 * </ul>
 */
@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
    @Inject(method = "isSmokeyPos", at = @At("HEAD"), cancellable = true)
    private static void beekeeper$isSmokeyPos(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (BeekeeperSmokeState.isSmoked(level, pos)) {
            cir.setReturnValue(true);
        }
    }
}