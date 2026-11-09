package net.sujjsteam.tweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.entity.Nucleon;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class NucleonRenderer extends GeoEntityRenderer<Nucleon> {

    public NucleonRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(Tweaks.MOD_ID, "nucleon"), true));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 1.2F;
    }

    @Override
    public void render(Nucleon boss, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(boss, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        NucleonShieldRenderer.draw(boss, partialTick, poseStack, bufferSource);
    }
}
