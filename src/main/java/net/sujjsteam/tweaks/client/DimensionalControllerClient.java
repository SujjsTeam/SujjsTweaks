package net.sujjsteam.tweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.item.DimensionalControllerItem;

import java.util.Random;

@EventBusSubscriber(modid = Tweaks.MOD_ID, value = Dist.CLIENT)
public class DimensionalControllerClient {

    private static final Random RANDOM = new Random();
    private static final int FLASH_TICKS = 16;

    private static float lastProgress;
    private static int lastColor = 0xB040FF;
    private static int flashTicks;
    private static int flashColor;

    private static float progress(Player player) {
        if (player == null || !player.isUsingItem()) {
            return 0.0F;
        }
        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof DimensionalControllerItem)) {
            return 0.0F;
        }
        CompoundTag tag = DimensionalControllerItem.data(stack);
        if (!tag.getBoolean(DimensionalControllerItem.ACTIVE)) {
            return 0.0F;
        }
        int charge = Math.max(1, tag.getInt(DimensionalControllerItem.CHARGE));
        lastColor = tag.getInt(DimensionalControllerItem.COLOR);
        return Math.min(1.0F, player.getTicksUsingItem() / (float) charge);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        float current = progress(player);
        if (current == 0.0F && lastProgress >= 0.95F) {
            flashTicks = FLASH_TICKS;
            flashColor = lastColor;
        }
        lastProgress = current;
        if (flashTicks > 0) {
            flashTicks--;
        }
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        float p = progress(event.getPlayer());
        if (p > 0.0F) {
            event.setNewFovModifier(event.getNewFovModifier() * (1.0F - 0.22F * p));
        }
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        float p = progress(Minecraft.getInstance().player);
        if (p > 0.3F) {
            float strength = (p - 0.3F) / 0.7F;
            float amount = 1.4F * strength * strength;
            event.setRoll(event.getRoll() + (RANDOM.nextFloat() - 0.5F) * 2.0F * amount);
            event.setYaw(event.getYaw() + (RANDOM.nextFloat() - 0.5F) * amount);
            event.setPitch(event.getPitch() + (RANDOM.nextFloat() - 0.5F) * amount);
        }
    }

    @SubscribeEvent
    public static void onGui(RenderGuiEvent.Post event) {
        float p = progress(Minecraft.getInstance().player);
        int width = event.getGuiGraphics().guiWidth();
        int height = event.getGuiGraphics().guiHeight();
        if (p > 0.0F) {
            int alpha = (int) (60 * p * p);
            event.getGuiGraphics().fill(0, 0, width, height, (alpha << 24) | (lastColor & 0xFFFFFF));
        }
        if (flashTicks > 0) {
            int alpha = (int) (220 * (flashTicks / (float) FLASH_TICKS));
            int rgb = flashTicks > FLASH_TICKS - 4 ? 0xFFFFFF : flashColor;
            event.getGuiGraphics().fill(0, 0, width, height, (alpha << 24) | (rgb & 0xFFFFFF));
        }
    }
}
