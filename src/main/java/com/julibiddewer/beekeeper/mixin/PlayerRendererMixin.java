package com.julibiddewer.beekeeper.mixin;

import com.julibiddewer.beekeeper.BeekeeperOutfitItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the player's outer skin layers (the second-layer hat/jacket/sleeves/pants) for
 * the body slots that currently hold a {@link BeekeeperOutfitItem} piece. Vanilla never
 * clears these layers for armor, so a slim skin layer pokes through the geo armor model
 * (most visibly the hat hair poking through the beekeeper hat brim). We hook the
 * third-person {@code PlayerRenderer#render} path right after vanilla's
 * {@code setModelProperties} has configured visibility and turn the matching layer parts
 * off, leaving first-person hand rendering untouched.
 *
 * <ul>
 *   <li>HELMET (head)     -> hat</li>
 *   <li>CHESTPLATE (chest)-> jacket + leftSleeve + rightSleeve</li>
 *   <li>LEGGINGS (legs)   -> leftPants + rightPants</li>
 *   <li>BOOTS (feet)      -> nothing (no separate outer skin layer below the ankles)</li>
 * </ul>
 * Other armor (vanilla iron/diamond, etc.) is left on Vanilla behavior - only Beekeeper
 * outfit pieces suppress the outer layer, per scope.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V",
            shift = At.Shift.AFTER
        )
    )
    private void beekeeper$hideOuterSkinLayers(
        AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci
    ) {
        PlayerModel<?> model = ((PlayerRenderer) (Object) this).getModel();

        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head.getItem() instanceof BeekeeperOutfitItem) {
            model.hat.visible = false;
        }

        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof BeekeeperOutfitItem) {
            model.jacket.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
        }

        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        if (legs.getItem() instanceof BeekeeperOutfitItem) {
            model.leftPants.visible = false;
            model.rightPants.visible = false;
        }
    }
}