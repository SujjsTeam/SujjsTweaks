package net.sujjsteam.tweaks.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.entity.IrradiatedSoldier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class IrradiatedSoldierRenderer extends GeoEntityRenderer<IrradiatedSoldier> {

    public IrradiatedSoldierRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(Tweaks.MOD_ID, "irradiated_soldier"), true));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 0.6F;
    }
}
