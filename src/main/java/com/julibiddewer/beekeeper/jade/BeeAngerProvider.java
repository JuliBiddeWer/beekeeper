package com.julibiddewer.beekeeper.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import com.julibiddewer.beekeeper.Beekeeper;

/**
 * Jade provider for bees: shows whether the bee is currently angry or calm. The Räuchergerät
 * pacifies angry bees by clearing their attack target and anger timer, so this is the
 * bee-level view of the smoker's effect (the timed smoke itself lives on the hive, not on
 * individual bees - see {@link SmokeHiveProvider} for the hive tooltip).
 */
public enum BeeAngerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Beekeeper.MODID, "bee_anger");
    private static final String KEY_ANGER = "BeekeeperBeeAnger";
    private static final String KEY_TARGET = "BeekeeperBeeHasTarget";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        Entity entity = accessor.getEntity();
        if (entity instanceof Bee bee) {
            data.putInt(KEY_ANGER, bee.getRemainingPersistentAngerTime());
            data.putBoolean(KEY_TARGET, bee.getTarget() != null);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(KEY_ANGER)) {
            int anger = data.getInt(KEY_ANGER);
            boolean hasTarget = data.getBoolean(KEY_TARGET);
            if (anger > 0 || hasTarget) {
                double seconds = anger / 20.0;
                tooltip.add(Component.translatable("tooltip.beekeeper.bee_angry",
                        String.format("%.1f", seconds)));
            } else {
                tooltip.add(Component.translatable("tooltip.beekeeper.bee_calm"));
            }
        }
    }
}