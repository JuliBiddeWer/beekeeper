package com.julibiddewer.beekeeper.client;

import com.julibiddewer.beekeeper.BeekeeperOutfitItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * GeckoLib armor renderer for the beekeeper outfit, used for the worn appearance.
 * GeoArmorRenderer automatically maps the geo.json bones named bipedHead/armorHead
 * (and the matching biped/armor bone pairs) onto the player's body parts and copies
 * the player's pose, so each outfit piece follows head yaw/pitch and body movement.
 * Per-slot bone visibility (head/body/legs/boots) is handled by GeckoLib based on the
 * item's ArmorItem.Type, so only the relevant bones show for the equipped piece.
 */
public class BeekeeperOutfitRenderer<T extends BeekeeperOutfitItem> extends GeoArmorRenderer<T> {
    public BeekeeperOutfitRenderer() {
        super(new BeekeeperOutfitModel<>());
    }
}