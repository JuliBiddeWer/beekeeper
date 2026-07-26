package com.julibiddewer.beekeeper;

import java.util.EnumMap;
import java.util.List;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Beekeeper.MODID)
public class Beekeeper {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "beekeeper";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold Items which will all be registered under the "beekeeper" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold ArmorMaterials
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, MODID);
    // Create a Deferred Register to hold CreativeModeTabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // The beekeeper armor material. Only the hat (HELMET) actually grants defense and enables the smoker;
    // the jacket, pants and boots are purely cosmetic, hence their defense is 0.
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BEEKEEPER_ARMOR_MATERIAL =
            ARMOR_MATERIALS.register("beekeeper", () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 0);
                        map.put(ArmorItem.Type.LEGGINGS, 0);
                        map.put(ArmorItem.Type.CHESTPLATE, 0);
                        map.put(ArmorItem.Type.HELMET, 2);
                        map.put(ArmorItem.Type.BODY, 0);
                    }),
                    15,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(Items.HONEYCOMB),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MODID, "beekeeper"))),
                    0.0F,
                    0.0F
            ));

    // Imkerhut - the only piece with an additional function: it is required to use the smoker on a hive.
    // Rendered as a real 3D hat via GeckoLib (BeekeeperOutfitItem implements GeoItem). The item id stays
    // "beekeeper_hat", so the smoker and bee-tracker checks that key on the item are unaffected.
    public static final DeferredItem<BeekeeperOutfitItem> BEEKEEPER_HAT =
            ITEMS.register("beekeeper_hat", () -> new BeekeeperOutfitItem(BEEKEEPER_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(7))));
    // Imkerjacke - cosmetic. Rendered via GeckoLib like the hat: when worn (CHEST slot) GeckoLib shows
    // the armorBody (+arm) bones of the shared beekeeper_outfit geo model, so the jacket's body cube appears.
    public static final DeferredItem<BeekeeperOutfitItem> BEEKEEPER_JACKET =
            ITEMS.register("beekeeper_jacket", () -> new BeekeeperOutfitItem(BEEKEEPER_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(7))));
    // Imkerhose - cosmetic. Rendered via GeckoLib: when worn (LEGS slot) GeckoLib shows the
    // armorLeftLeg / armorRightLeg bones of the shared beekeeper_outfit geo model.
    public static final DeferredItem<BeekeeperOutfitItem> BEEKEEPER_PANTS =
            ITEMS.register("beekeeper_pants", () -> new BeekeeperOutfitItem(BEEKEEPER_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(7))));
    // Imkerstiefel - cosmetic. Rendered via GeckoLib: when worn (FEET slot) GeckoLib shows the
    // armorLeftBoot / armorRightBoot bones of the shared beekeeper_outfit geo model.
    public static final DeferredItem<BeekeeperOutfitItem> BEEKEEPER_BOOTS =
            ITEMS.register("beekeeper_boots", () -> new BeekeeperOutfitItem(BEEKEEPER_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(7))));

    // Räuchergerät - applies smoke to beehives / bee nests when used together with the beekeeper hat.
    // Durability matches flint & steel (65); it can be repaired with iron ingots and enchanted
    // (Unbreaking via the vanilla enchantable/durability tag, plus our smoker-exclusive enchantments).
    public static final DeferredItem<Item> SMOKER =
            ITEMS.register("smoker", () -> new SmokerItem(new Item.Properties().stacksTo(1).durability(65).rarity(Rarity.UNCOMMON)));

    // Creative tab for all beekeeper items, placed after the combat tab.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BEEKEEPER_TAB =
            CREATIVE_MODE_TABS.register("beekeeper_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.beekeeper"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(SMOKER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(BEEKEEPER_HAT.get());
                        output.accept(BEEKEEPER_JACKET.get());
                        output.accept(BEEKEEPER_PANTS.get());
                        output.accept(BEEKEEPER_BOOTS.get());
                        output.accept(SMOKER.get());
                    }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    public Beekeeper(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);
        // The bee-tracker hat enchantment is applied each server tick. (The enchantments
        // themselves are datapack-registry entries, defined as JSON in data/beekeeper/enchantment/.)
        NeoForge.EVENT_BUS.register(new BeeTrackerHandler());

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Beekeeper mod ready.");
    }
}