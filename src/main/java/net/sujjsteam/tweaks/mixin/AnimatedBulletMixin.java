package net.sujjsteam.tweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.sujjsteam.tweaks.compat.scguns.AmmoBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.GeoBone;
import top.ribs.scguns.client.render.gun.animated.AnimatedGunRenderer;
import top.ribs.scguns.item.animated.AnimatedGunItem;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mixin(value = AnimatedGunRenderer.class, remap = false)
public abstract class AnimatedBulletMixin {

    @Unique
    private static final Set<String> TWEAKS_BULLET_BONES = Set.of("shell", "shell2", "bullet", "bullet2");

    @Unique
    private static final Set<GeoBone> TWEAKS_HIDDEN_BY_US = Collections.newSetFromMap(new WeakHashMap<>());

    @Shadow
    private ItemStack currentRenderStack;

    @Inject(method = "renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Ltop/ribs/scguns/item/animated/AnimatedGunItem;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V",
            at = @At("HEAD"))
    private void tweaks$hideAnimatedBullet(PoseStack poseStack, AnimatedGunItem animatable, GeoBone bone, RenderType renderType,
                                           MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                           float partialTick, int packedLight, int packedOverlay, int colour, CallbackInfo ci) {
        if (bone == null || !TWEAKS_BULLET_BONES.contains(bone.getName())) {
            return;
        }
        if (AmmoBridge.hideAnimatedBullet(Minecraft.getInstance().player, currentRenderStack)) {
            if (!bone.isHidden()) {
                bone.setHidden(true);
                TWEAKS_HIDDEN_BY_US.add(bone);
            }
        } else if (TWEAKS_HIDDEN_BY_US.remove(bone)) {
            bone.setHidden(false);
        }
    }
}
