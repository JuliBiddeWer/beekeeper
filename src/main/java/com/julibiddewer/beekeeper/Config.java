package com.julibiddewer.beekeeper;

import net.neoforged.neoforge.common.ModConfigSpec;

// Config for the Beekeeper mod. NeoForge creates and loads the file (config/beekeeper-common.toml)
// automatically; it can also be edited in-game via Mods > Beekeeper > Config.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SMOKE_DURATION_TICKS = BUILDER
            .comment(
                    "How long (in game ticks; 20 ticks = 1 second) a beehive / bee nest stays",
                    "calmed after being smoked with the Räuchergerät. While calmed, bees do not",
                    "become aggressive when honey is harvested or the hive is broken.",
                    "Default: 1200 (60 seconds)."
            )
            .defineInRange("smokeDurationTicks", 1200, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue BASE_PACIFY_RADIUS = BUILDER
            .comment(
                    "Radius (in blocks) around the smoked hive / player in which already angry",
                    "bees are calmed (their target is cleared and they become neutral again).",
                    "Default: 8.0."
            )
            .defineInRange("basePacifyRadius", 8.0, 1.0, 64.0);

    public static final ModConfigSpec.IntValue BASE_COOLDOWN_TICKS = BUILDER
            .comment(
                    "Cooldown (in ticks; 20 ticks = 1 second) between smoker uses before any",
                    "Quick Stoking enchantment is applied. Default: 20 (1 second)."
            )
            .defineInRange("baseCooldownTicks", 20, 0, 200);

    public static final ModConfigSpec.IntValue RADIUS_BONUS_PER_LEVEL = BUILDER
            .comment(
                    "Blocks added to the pacify radius per level of the Smoke Radius enchantment.",
                    "Default: 2."
            )
            .defineInRange("radiusBonusPerLevel", 2, 0, 32);

    public static final ModConfigSpec.IntValue DURATION_BONUS_PER_LEVEL = BUILDER
            .comment(
                    "Ticks added to the hive smoke duration per level of the Lingering Smoke",
                    "enchantment (added on top of smokeDurationTicks). Default: 400 (20 seconds)."
            )
            .defineInRange("durationBonusPerLevel", 400, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue COOLDOWN_REDUCTION_PER_LEVEL = BUILDER
            .comment(
                    "Ticks removed from the use cooldown per level of the Quick Stoking",
                    "enchantment. The cooldown never drops below minCooldownTicks.",
                    "Default: 4."
            )
            .defineInRange("cooldownReductionPerLevel", 4, 0, 100);

    public static final ModConfigSpec.IntValue MIN_COOLDOWN_TICKS = BUILDER
            .comment(
                    "Hard floor for the smoker use cooldown, no matter how high Quick Stoking",
                    "is stacked. Default: 4 ticks."
            )
            .defineInRange("minCooldownTicks", 4, 0, 200);

    static final ModConfigSpec SPEC = BUILDER.build();
}