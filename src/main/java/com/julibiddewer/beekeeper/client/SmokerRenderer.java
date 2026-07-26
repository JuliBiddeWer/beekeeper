package com.julibiddewer.beekeeper.client;

import com.julibiddewer.beekeeper.SmokerItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * GeckoLib item renderer for the smoker. GeoItemRenderer extends the vanilla
 * {@code BlockEntityWithoutLevelRenderer}, so the smoker's 3D geo model renders in the inventory,
 * on the ground, in item frames, and in first/third-person hands. Per-perspective placement
 * (translation per display context) is applied via the {@code builtin/entity} item model json,
 * which Minecraft honours before delegating to this renderer.
 */
public class SmokerRenderer<T extends SmokerItem> extends GeoItemRenderer<T> {
    public SmokerRenderer() {
        super(new SmokerModel<>());
    }
}