package net.sujjsteam.tweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.world.RadioactiveDimension;

@EventBusSubscriber(modid = Tweaks.MOD_ID, value = Dist.CLIENT)
public class RadioactiveDimensionClient {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (!RadioactiveDimension.DIMENSION.equals(minecraft.level.dimension()) || !minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            return;
        }
        minecraft.gameRenderer.processBlurEffect(minecraft.getTimer().getGameTimeDeltaPartialTick(false));
        minecraft.getMainRenderTarget().bindWrite(false);
    }
}
