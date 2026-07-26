package com.julibiddewer.beekeeper;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import com.julibiddewer.beekeeper.client.SmokerRenderer;

import java.util.function.Consumer;

/**
 * The Räuchergerät (smoker). Right-clicking a beehive or bee nest while wearing the beekeeper
 * hat applies smoke to that hive. A smoked hive behaves exactly as if a campfire were burning
 * underneath it: bees do not become aggressive when honey is harvested or when the hive is
 * broken (handled by the mixins which make the hive report itself as "sedated" and which cancel
 * the "anger nearby bees" call). In addition, smoking <em>calms already angry bees</em> in the
 * area - they drop their target and become neutral again. Right-clicking the air releases a
 * puff of smoke and likewise calms nearby angry bees.
 *
 * <p>The smoker has durability (65, like flint &amp; steel). Each use costs one durability point,
 * reduced automatically by the vanilla Unbreaking enchantment (the smoker is added to the
 * {@code minecraft:enchantable/durability} tag). Three smoker-exclusive enchantments scale its
 * behaviour: {@link BeekeeperEnchantments#SMOKE_RADIUS} widens the pacify radius,
 * {@link BeekeeperEnchantments#SMOKE_DURATION} lengthens the hive smoke, and
 * {@link BeekeeperEnchantments#QUICK_COOLDOWN} shortens the use cooldown.</p>
 */
public class SmokerItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public SmokerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<SmokerItem> itemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new SmokerRenderer<>();
                }
                return this.itemRenderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations yet - the smoker is static.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public int getEnchantmentValue() {
        // Lets the smoker go into the enchanting table (and accept Unbreaking / our custom enchants).
        return 14;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.IRON_INGOT);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof BeehiveBlock)) {
            return super.useOn(context);
        }

        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        // The beekeeper hat is the key that lets the smoker calm a hive.
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(Beekeeper.BEEKEEPER_HAT.get())) {
            return super.useOn(context);
        }

        ItemStack stack = context.getItemInHand();
        int radiusLevel = BeekeeperEnchantments.getLevel(level.registryAccess(), stack, BeekeeperEnchantments.SMOKE_RADIUS);
        int durationLevel = BeekeeperEnchantments.getLevel(level.registryAccess(), stack, BeekeeperEnchantments.SMOKE_DURATION);
        int cooldownLevel = BeekeeperEnchantments.getLevel(level.registryAccess(), stack, BeekeeperEnchantments.QUICK_COOLDOWN);

        long duration = Config.SMOKE_DURATION_TICKS.get() + (long) durationLevel * Config.DURATION_BONUS_PER_LEVEL.get();
        double radius = Config.BASE_PACIFY_RADIUS.get() + radiusLevel * Config.RADIUS_BONUS_PER_LEVEL.get();

        BeekeeperSmokeState.applySmoke(level, pos, duration);
        pacifyNearbyBees(level, Vec3.atCenterOf(pos), radius);

        if (level instanceof ServerLevel serverLevel) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 24, 0.4, 0.3, 0.4, 0.01);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 12, 0.3, 0.2, 0.3, 0.02);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);

            // One durability per use (Unbreaking rolls automatically inside hurtAndBreak).
            stack.hurtAndBreak(1, serverLevel, player, item -> {});
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCooldowns().addCooldown(this, cooldownTicks(cooldownLevel));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int radiusLevel = BeekeeperEnchantments.getLevel(level.registryAccess(), stack, BeekeeperEnchantments.SMOKE_RADIUS);
        int cooldownLevel = BeekeeperEnchantments.getLevel(level.registryAccess(), stack, BeekeeperEnchantments.QUICK_COOLDOWN);
        double radius = Config.BASE_PACIFY_RADIUS.get() + radiusLevel * Config.RADIUS_BONUS_PER_LEVEL.get();

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX(), player.getEyeY() - 0.2, player.getZ(),
                    8, 0.3, 0.1, 0.3, 0.01);
            level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 1.0F);
            // Smoking the air also calms already angry bees around the player.
            pacifyNearbyBees(level, player.position().add(0.0, 1.0, 0.0), radius);
            stack.hurtAndBreak(1, serverLevel, player, item -> {});
        }

        player.getCooldowns().addCooldown(this, cooldownTicks(cooldownLevel));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static int cooldownTicks(int cooldownLevel) {
        return Math.max(Config.MIN_COOLDOWN_TICKS.get(),
                Config.BASE_COOLDOWN_TICKS.get() - cooldownLevel * Config.COOLDOWN_REDUCTION_PER_LEVEL.get());
    }

    /**
     * Finds every bee within {@code radius} of {@code center} that is currently angry and
     * calms it: clears its attack target and resets the persistent-anger timer so it becomes
     * neutral again. A small puff of smoke is spawned on each calmed bee as feedback.
     */
    private static void pacifyNearbyBees(Level level, Vec3 center, double radius) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB box = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        List<Bee> bees = serverLevel.getEntitiesOfClass(Bee.class, box);
        for (Bee bee : bees) {
            if (bee.getTarget() == null && bee.getRemainingPersistentAngerTime() <= 0) {
                continue;
            }
            bee.setTarget(null);
            bee.setRemainingPersistentAngerTime(0);
            bee.setPersistentAngerTarget(null);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    bee.getX(), bee.getY(0.5), bee.getZ(), 4, 0.2, 0.2, 0.2, 0.01);
        }
    }
}