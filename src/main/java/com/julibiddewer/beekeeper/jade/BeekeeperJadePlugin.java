package com.julibiddewer.beekeeper.jade;

import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade ("What am I looking at") integration for the Beekeeper mod.
 * <p>
 * Adds two pieces of information to the Jade tooltip:
 * <ul>
 *   <li>When looking at a beehive or bee nest, shows whether it is still smoked by the
 *       Räuchergerät and for how long (the remaining smoke time). This data lives only on
 *       the server (see {@link com.julibiddewer.beekeeper.BeekeeperSmokeState}), so it is
 *       sent to the client via an {@link snownee.jade.api.IServerDataProvider}.</li>
 *   <li>When looking at a bee, shows its anger state (angry / calm). The smoker pacifies
 *       angry bees by clearing their target and anger timer, so a "calm" bee is one that
 *       has been (or simply is) neutral.</li>
 * </ul>
 * <p>
 * Jade discovers this plugin via the {@link WailaPlugin} annotation. The annotation is
 * only scanned when Jade is present at runtime, so when Jade is absent this class is never
 * loaded and the mod keeps working without it.
 */
@WailaPlugin
public class BeekeeperJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Server-side data providers. BeehiveBlockEntity covers both the beehive and the
        // bee nest (both are BeehiveBlock instances sharing the same block entity type).
        registration.registerBlockDataProvider(SmokeHiveProvider.INSTANCE, BeehiveBlockEntity.class);
        registration.registerEntityDataProvider(BeeAngerProvider.INSTANCE, Bee.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Client-side tooltip components. BeehiveBlock.class covers both beehive and bee nest.
        registration.registerBlockComponent(SmokeHiveProvider.INSTANCE, BeehiveBlock.class);
        registration.registerEntityComponent(BeeAngerProvider.INSTANCE, Bee.class);
    }
}