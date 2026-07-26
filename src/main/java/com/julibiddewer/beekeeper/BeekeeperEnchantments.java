package com.julibiddewer.beekeeper;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Custom enchantments for the Beekeeper mod.
 * <p>
 * In Minecraft 1.21.1 the enchantment registry is a <em>datapack registry</em>: enchantments are
 * defined as JSON files in {@code data/beekeeper/enchantment/*.json} and loaded with the world's
 * datapacks. They are <strong>not</strong> registered through a {@code DeferredRegister}. This
 * class therefore only holds the {@link ResourceKey}s (matching the JSON file names) plus a few
 * tuning constants, and resolves the actual {@link Holder} lazily at runtime via the level's
 * {@link RegistryAccess}.
 * <ul>
 *   <li>{@link #SMOKE_RADIUS}, {@link #SMOKE_DURATION}, {@link #QUICK_COOLDOWN} are exclusive to
 *       the smoker (their {@code supported_items} tag lists only the smoker).</li>
 *   <li>{@link #BEE_TRACKER} is exclusive to the beekeeper hat (applied in {@link BeeTrackerHandler}).</li>
 * </ul>
 * These enchantments are "statless" - they carry no data-driven effects; their behaviour is read
 * from the enchantment level in {@link SmokerItem} / {@link BeeTrackerHandler}.
 */
public final class BeekeeperEnchantments {
    // --- Smoker-exclusive enchantments ----------------------------------------

    public static final ResourceKey<Enchantment> SMOKE_RADIUS = create("smoke_radius");
    public static final ResourceKey<Enchantment> SMOKE_DURATION = create("smoke_duration");
    public static final ResourceKey<Enchantment> QUICK_COOLDOWN = create("quick_cooldown");

    // --- Hat enchantment ------------------------------------------------------

    public static final ResourceKey<Enchantment> BEE_TRACKER = create("bee_tracker");

    private BeekeeperEnchantments() {
    }

    private static ResourceKey<Enchantment> create(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(Beekeeper.MODID, name));
    }

    /**
     * Returns the level of {@code key} on {@code stack}, or {@code 0} if the enchantment is not
     * present (either on the item or in the loaded registry). Null-safe: the enchantment registry
     * is a datapack registry and is only populated after datapacks load, and this method may be
     * called on the client side too, so it must never throw when the enchantment is absent.
     */
    public static int getLevel(RegistryAccess access, ItemStack stack, ResourceKey<Enchantment> key) {
        return access.lookup(Registries.ENCHANTMENT)
                .flatMap(lookup -> lookup.get(key))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }
}