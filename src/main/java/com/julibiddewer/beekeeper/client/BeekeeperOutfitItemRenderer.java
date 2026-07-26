package com.julibiddewer.beekeeper.client;

import com.julibiddewer.beekeeper.BeekeeperOutfitItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * GeckoLib item renderer for the beekeeper outfit pieces, used for the inventory / held / ground /
 * item-frame icon. The outfit is one shared geo model containing every piece; this renderer shows
 * only the {@code armor*} bones that belong to the item's equipment slot (mirroring what
 * {@link BeekeeperOutfitRenderer} does for the worn appearance), so each piece's icon is just that
 * piece and not the whole outfit.
 *
 * <p>{@code setHidden(true)} on a {@code GeoBone} hides the bone and its descendants, so hiding the
 * eight {@code armor*} bones first and then un-hiding the slot's bones leaves exactly the right
 * piece visible. The {@code biped*} parent bones hold no cubes and stay visible, so the slot's
 * armor bone is still reached during render recursion.</p>
 */
public class BeekeeperOutfitItemRenderer<T extends BeekeeperOutfitItem> extends GeoItemRenderer<T> {
    public BeekeeperOutfitItemRenderer() {
        super(new BeekeeperOutfitModel<>());
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, int renderType) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, renderType);
        applySlotVisibility(model, animatable);
    }

    private void applySlotVisibility(BakedGeoModel model, T animatable) {
        EquipmentSlot slot = animatable.getEquipmentSlot();

        // Hide every armor bone (and its children) first, then un-hide the slot's bones.
        setVisible(model, "armorHead", false);
        setVisible(model, "armorBody", false);
        setVisible(model, "armorRightArm", false);
        setVisible(model, "armorLeftArm", false);
        setVisible(model, "armorRightLeg", false);
        setVisible(model, "armorLeftLeg", false);
        setVisible(model, "armorRightBoot", false);
        setVisible(model, "armorLeftBoot", false);

        switch (slot) {
            case HEAD -> setVisible(model, "armorHead", true);
            case CHEST -> {
                setVisible(model, "armorBody", true);
                setVisible(model, "armorRightArm", true);
                setVisible(model, "armorLeftArm", true);
            }
            case LEGS -> {
                setVisible(model, "armorRightLeg", true);
                setVisible(model, "armorLeftLeg", true);
            }
            case FEET -> {
                setVisible(model, "armorRightBoot", true);
                setVisible(model, "armorLeftBoot", true);
            }
            default -> { }
        }
    }

    private static void setVisible(BakedGeoModel model, String boneName, boolean visible) {
        model.getBone(boneName).ifPresent(bone -> bone.setHidden(!visible));
    }
}