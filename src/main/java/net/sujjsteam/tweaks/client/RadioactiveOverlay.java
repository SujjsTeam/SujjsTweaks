package net.sujjsteam.tweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.registry.ModEffects;

@EventBusSubscriber(modid = Tweaks.MOD_ID, value = Dist.CLIENT)
public class RadioactiveOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.hasEffect(ModEffects.RADIOACTIVE)) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float time = minecraft.player.tickCount + event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float pulse = 0.5F + 0.5F * Mth.sin(time * 0.15F);
        int baseAlpha = (int) (40 + 25 * pulse);
        int edgeAlpha = (int) (110 + 50 * pulse);
        int green = 0x39FF14;
        graphics.fill(0, 0, width, height, (baseAlpha << 24) | green);
        int band = height / 4;
        graphics.fillGradient(0, 0, width, band, (edgeAlpha << 24) | green, green);
        graphics.fillGradient(0, height - band, width, height, green, (edgeAlpha << 24) | green);
    }
}
