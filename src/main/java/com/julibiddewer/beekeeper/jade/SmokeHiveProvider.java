package com.julibiddewer.beekeeper.jade;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import com.julibiddewer.beekeeper.Beekeeper;
import com.julibiddewer.beekeeper.BeekeeperSmokeState;

/**
 * Jade provider for beehives / bee nests: shows the remaining smoke time of the Räuchergerät.
 * Implements both the server data provider (computes the remaining ticks on the server, where
 * {@link BeekeeperSmokeState} lives) and the client component provider (renders the tooltip).
 */
public enum SmokeHiveProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Beekeeper.MODID, "smoke_hive");
    private static final String KEY_REMAINING = "BeekeeperSmokeRemaining";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        long remaining = BeekeeperSmokeState.getRemainingTicks(level, pos);
        if (remaining > 0) {
            data.putLong(KEY_REMAINING, remaining);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(KEY_REMAINING)) {
            long remaining = data.getLong(KEY_REMAINING);
            if (remaining > 0) {
                double seconds = remaining / 20.0;
                tooltip.add(Component.translatable("tooltip.beekeeper.smoke_remaining",
                        String.format("%.1f", seconds)));
            }
        }
    }
}