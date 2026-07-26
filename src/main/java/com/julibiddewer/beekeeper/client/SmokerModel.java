package com.julibiddewer.beekeeper.client;

import com.julibiddewer.beekeeper.Beekeeper;
import com.julibiddewer.beekeeper.SmokerItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for the smoker item. Points at the Bedrock geometry file
 * {@code smoker.geo.json} and the smoker texture {@code smoker.png}. No animation file is used
 * yet (getAnimationResource returns null), so the smoker renders statically; GeckoLib handles
 * a null animation gracefully.
 */
public class SmokerModel<T extends SmokerItem> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return ResourceLocation.fromNamespaceAndPath(Beekeeper.MODID, "geo/smoker.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return ResourceLocation.fromNamespaceAndPath(Beekeeper.MODID, "textures/item/smoker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return null;
    }
}