package net.sujjsteam.tweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.ritual.RitualSky;

import java.util.List;

@EventBusSubscriber(modid = Tweaks.MOD_ID, value = Dist.CLIENT)
public class RitualSkyClient {

    private static final float FULL_EFFECT_DISTANCE = 64.0F;
    private static final float MAX_DISTANCE = 128.0F;

    private static float strength;
    private static float flash;

    public static float strength() {
        return strength;
    }

    public static float flash() {
        return flash;
    }

    @SubscribeEvent
    public static void onClientTickEnd(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused()) {
            return;
        }
        List<RitualSky.Request> requests = RitualSky.takeRequests();
        float target = 0.0F;
        float flashTarget = 0.0F;
        if (minecraft.player != null) {
            for (RitualSky.Request request : requests) {
                double dist = Math.sqrt(minecraft.player.distanceToSqr(request.x(), request.y(), request.z()));
                float near = Mth.clamp((float) ((MAX_DISTANCE - dist) / (MAX_DISTANCE - FULL_EFFECT_DISTANCE)), 0.0F, 1.0F);
                target = Math.max(target, request.strength() * near);
                flashTarget = Math.max(flashTarget, request.flash() * near);
            }
        }
        strength += (target - strength) * (target > strength ? 0.05F : 0.025F);
        if (strength < 0.001F) {
            strength = 0.0F;
        }
        flash = Math.max(flashTarget, flash * 0.85F);
        if (flash < 0.001F) {
            flash = 0.0F;
        }
    }

    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor event) {
        if (strength <= 0.0F && flash <= 0.0F) {
            return;
        }
        float r = Mth.lerp(strength, event.getRed(), 0.07F);
        float g = Mth.lerp(strength, event.getGreen(), 0.30F);
        float b = Mth.lerp(strength, event.getBlue(), 0.06F);
        event.setRed(Mth.lerp(flash, r, 0.65F));
        event.setGreen(Mth.lerp(flash, g, 1.0F));
        event.setBlue(Mth.lerp(flash, b, 0.5F));
    }

    @SubscribeEvent
    public static void fogDistance(ViewportEvent.RenderFog event) {
        if (strength <= 0.0F) {
            return;
        }
        float near = event.getNearPlaneDistance();
        float far = event.getFarPlaneDistance();
        if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            event.setNearPlaneDistance(Mth.lerp(strength, near, 0.0F));
            event.setFarPlaneDistance(Mth.lerp(strength * 0.85F, far, 8.0F));
        } else {
            event.setNearPlaneDistance(Mth.lerp(strength * 0.7F, near, 0.0F));
            event.setFarPlaneDistance(Mth.lerp(strength, far, Math.max(32.0F, far * 0.45F)));
        }
        event.setCanceled(true);
    }
}
