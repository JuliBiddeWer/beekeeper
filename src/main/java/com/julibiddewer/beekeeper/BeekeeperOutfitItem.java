package com.julibiddewer.beekeeper;

import com.julibiddewer.beekeeper.client.BeekeeperOutfitItemRenderer;
import com.julibiddewer.beekeeper.client.BeekeeperOutfitRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Consumer;

/**
 * GeckoLib-driven armor item for the beekeeper outfit. Each piece of the outfit
 * (hat/jacket/pants/boots) is a {@link BeekeeperOutfitItem}; they all share the same
 * {@code beekeeper_outfit} geo model and renderer. GeckoLib's {@code GeoArmorRenderer}
 * maps the geo bones named bipedHead/armorHead, bipedBody/armorBody, etc. onto the
 * player and, via {@code applyBoneVisibilityBySlot}, shows only the bones that belong
 * to the item's {@link ArmorItem.Type}:
 * <ul>
 *   <li>HELMET  -> armorHead</li>
 *   <li>CHESTPLATE -> armorBody (+ arms)</li>
 *   <li>LEGGINGS -> armorLeftLeg / armorRightLeg</li>
 *   <li>BOOTS   -> armorLeftBoot / armorRightBoot</li>
 * </ul>
 * <p>
 */
public class BeekeeperOutfitItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public BeekeeperOutfitItem(Holder<ArmorMaterial> material, Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<BeekeeperOutfitItem> armorRenderer;
            private GeoItemRenderer<BeekeeperOutfitItem> itemRenderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.armorRenderer == null) {
                    this.armorRenderer = new BeekeeperOutfitRenderer<>();
                }
                return this.armorRenderer;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new BeekeeperOutfitItemRenderer<>();
                }
                return this.itemRenderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations yet - the outfit is static.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}