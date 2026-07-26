package com.julibiddewer.beekeeper;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Tracks which beehives / bee nests are currently "smoked" by the Räuchergerät.
 * <p>
 * State is kept in memory and keyed by {@link Level} (via a {@link WeakHashMap} so unloaded
 * dimensions do not leak) and the hive's {@link BlockPos}. An absolute expiry game-time is
 * stored, so no per-tick cleanup is required: entries are lazily evicted when queried after
 * they have expired.
 */
public final class BeekeeperSmokeState {
    private static final Map<Level, Map<BlockPos, Long>> SMOKED = new WeakHashMap<>();

    private BeekeeperSmokeState() {
    }

    public static void applySmoke(Level level, BlockPos pos, long durationTicks) {
        if (level == null || level.isClientSide) {
            return;
        }
        long expiry = level.getGameTime() + durationTicks;
        SMOKED.computeIfAbsent(level, l -> new HashMap<>()).merge(pos.immutable(), expiry, Math::max);
    }

    public static boolean isSmoked(Level level, BlockPos pos) {
        return getRemainingTicks(level, pos) > 0;
    }

    /**
     * Returns how many game ticks the hive at {@code pos} will stay smoked for, or a value
     * {@code <= 0} if it is not currently smoked. Lazily evicts expired entries. Used by the
     * Jade tooltip integration to display the remaining smoke time.
     */
    public static long getRemainingTicks(Level level, BlockPos pos) {
        if (level == null) {
            return -1;
        }
        Map<BlockPos, Long> map = SMOKED.get(level);
        if (map == null) {
            return -1;
        }
        Long expiry = map.get(pos);
        if (expiry == null) {
            return -1;
        }
        long remaining = expiry - level.getGameTime();
        if (remaining <= 0) {
            map.remove(pos);
            return -1;
        }
        return remaining;
    }
}