package net.sujjsteam.tweaks.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.sujjsteam.tweaks.compat.scguns.AmmoBridge;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.ribs.scguns.client.handler.HUDRenderHandler;

@Mixin(value = HUDRenderHandler.class, remap = false)
public abstract class HudAmmoMixin {

    @Shadow
    private static boolean cachedHasCreativeBox;

    @ModifyArg(method = "renderGunInfoHUD",
            at = @At(value = "INVOKE", target = "Ltop/ribs/scguns/client/handler/HUDRenderHandler;renderAmmoTypeTexture(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/Minecraft;)V"),
            index = 0)
    private static ItemStack tweaks$ammoIcon(ItemStack original) {
        return AmmoBridge.displayIcon(Minecraft.getInstance().player, original);
    }

    @Redirect(method = "renderGunInfoHUD",
            at = @At(value = "FIELD", target = "Ltop/ribs/scguns/client/handler/HUDRenderHandler;cachedHasCreativeBox:Z", opcode = Opcodes.GETSTATIC))
    private static boolean tweaks$infiniteReserve() {
        return cachedHasCreativeBox || AmmoBridge.hasInfinite(Minecraft.getInstance().player);
    }
}
