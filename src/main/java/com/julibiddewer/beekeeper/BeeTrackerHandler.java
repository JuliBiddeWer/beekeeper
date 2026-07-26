package com.julibiddewer.beekeeper;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Server-side handler for the "Bee Tracker" hat enchantment. While a player wears a beekeeper
 * hat enchanted with {@link BeekeeperEnchantments#BEE_TRACKER}, every bee within tracking range
 * is given a short, particle-free Glowing effect so its outline shows through walls.
 * <p>
 * The effect is refreshed a few times per second (rather than every tick) to keep it cheap;
 * the duration is kept slightly longer than the refresh interval so the outline never flickers.
 */
public class BeeTrackerHandler {
    private static final double TRACK_RADIUS = 48.0;
    private static final int REFRESH_EVERY_TICKS = 10;
    private static final int EFFECT_DURATION = 30; // ticks - comfortably longer than the refresh interval

    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter < REFRESH_EVERY_TICKS) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (BeekeeperEnchantments.getLevel(player.registryAccess(), helmet, BeekeeperEnchantments.BEE_TRACKER) <= 0) {
                continue;
            }

            AABB box = new AABB(
                    player.getX() - TRACK_RADIUS, player.getY() - TRACK_RADIUS, player.getZ() - TRACK_RADIUS,
                    player.getX() + TRACK_RADIUS, player.getY() + TRACK_RADIUS, player.getZ() + TRACK_RADIUS);
            List<Bee> bees = player.level().getEntitiesOfClass(Bee.class, box);
            for (Bee bee : bees) {
                // ambient=false, particles=false, visible=false: no potion particles or HUD icon,
                // only the glowing outline (which renders regardless of these flags).
                bee.addEffect(new MobEffectInstance(MobEffects.GLOWING, EFFECT_DURATION, 0, false, false, false));
            }
        }
    }
}