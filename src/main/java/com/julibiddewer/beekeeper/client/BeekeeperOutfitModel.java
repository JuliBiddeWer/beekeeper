package com.julibiddewer.beekeeper.client;

import com.julibiddewer.beekeeper.Beekeeper;
import com.julibiddewer.beekeeper.BeekeeperOutfitItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for the beekeeper outfit. Points at the shared Bedrock geometry file
 * {@code beekeeper_outfit.geo.json} and the outfit texture {@code beekeeper_outfit.png}.
 * No animation file is used yet (getAnimationResource returns null), so the outfit renders
 * statically; GeckoLib handles a null animation gracefully.
 */
public class BeekeeperOutfitModel<T extends BeekeeperOutfitItem> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return ResourceLocation.fromNamespaceAndPath(Beekeeper.MODID, "geo/beekeeper_outfit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return ResourceLocation.fromNamespaceAndPath(Beekeeper.MODID, "textures/armor/beekeeper_outfit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return null;
    }
}