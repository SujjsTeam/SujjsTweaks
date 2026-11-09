package net.sujjsteam.tweaks.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.client.renderer.AltarRadioactiveRenderer;
import net.sujjsteam.tweaks.client.renderer.IrradiatedSoldierRenderer;
import net.sujjsteam.tweaks.client.renderer.NucleonRenderer;
import net.sujjsteam.tweaks.registry.ModBlockEntities;
import net.sujjsteam.tweaks.registry.ModEntities;

@EventBusSubscriber(modid = Tweaks.MOD_ID, value = Dist.CLIENT)
public class TweaksClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ALTAR_RADIOACTIVE.get(), AltarRadioactiveRenderer::new);
        event.registerEntityRenderer(ModEntities.NUCLEON.get(), NucleonRenderer::new);
        event.registerEntityRenderer(ModEntities.IRRADIATED_SOLDIER.get(), IrradiatedSoldierRenderer::new);
        event.registerEntityRenderer(ModEntities.RADIOACTIVE_ORB.get(), context -> new ThrownItemRenderer<>(context, 2.0F, true));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(WindowIcon::apply);
    }
}
