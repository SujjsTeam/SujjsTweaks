package net.sujjsteam.tweaks.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.sujjsteam.tweaks.compat.scguns.AmmoBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.common.AmmoContext;
import top.ribs.scguns.common.Gun;

import java.util.Arrays;

@Mixin(value = Gun.class, remap = false)
public abstract class GunAmmoMixin {

    private static Item tweaks$defaultAmmo(Gun gun) {
        if (gun == null || gun.getProjectile() == null) {
            return null;
        }
        return gun.getProjectile().getItem();
    }

    private static boolean tweaks$empty(ItemStack[] stacks) {
        if (stacks == null) {
            return true;
        }
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Inject(method = "findAmmo(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)Ltop/ribs/scguns/common/AmmoContext;",
            at = @At("HEAD"), cancellable = true)
    private static void tweaks$infiniteByItem(Player player, Item item, CallbackInfoReturnable<AmmoContext> cir) {
        if (AmmoBridge.hasInfinite(player)) {
            ItemStack stack = AmmoBridge.infiniteStack(player, item);
            if (stack != null) {
                cir.setReturnValue(new AmmoContext(stack, null));
            }
        }
    }

    @Inject(method = "findAmmo(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)Ltop/ribs/scguns/common/AmmoContext;",
            at = @At("RETURN"), cancellable = true)
    private static void tweaks$universalByItem(Player player, Item item, CallbackInfoReturnable<AmmoContext> cir) {
        AmmoContext found = cir.getReturnValue();
        if (found == null || found.stack().isEmpty()) {
            ItemStack stack = AmmoBridge.universalStack(player, item);
            if (stack != null) {
                cir.setReturnValue(new AmmoContext(stack, null));
            }
        }
    }

    @Inject(method = "findAmmo(Lnet/minecraft/world/entity/player/Player;Ltop/ribs/scguns/common/Gun;)Ltop/ribs/scguns/common/AmmoContext;",
            at = @At("HEAD"), cancellable = true)
    private static void tweaks$infiniteByGun(Player player, Gun gun, CallbackInfoReturnable<AmmoContext> cir) {
        if (AmmoBridge.hasInfinite(player)) {
            ItemStack stack = AmmoBridge.infiniteStack(player, tweaks$defaultAmmo(gun));
            if (stack != null) {
                cir.setReturnValue(new AmmoContext(stack, null));
            }
        }
    }

    @Inject(method = "findAmmo(Lnet/minecraft/world/entity/player/Player;Ltop/ribs/scguns/common/Gun;)Ltop/ribs/scguns/common/AmmoContext;",
            at = @At("RETURN"), cancellable = true)
    private static void tweaks$universalByGun(Player player, Gun gun, CallbackInfoReturnable<AmmoContext> cir) {
        AmmoContext found = cir.getReturnValue();
        if (found == null || found.stack().isEmpty()) {
            ItemStack stack = AmmoBridge.universalStack(player, tweaks$defaultAmmo(gun));
            if (stack != null) {
                cir.setReturnValue(new AmmoContext(stack, null));
            }
        }
    }

    @Inject(method = "findAmmoStack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)[Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"), cancellable = true)
    private static void tweaks$infiniteStacks(Player player, Item item, CallbackInfoReturnable<ItemStack[]> cir) {
        if (AmmoBridge.hasInfinite(player)) {
            ItemStack stack = AmmoBridge.infiniteStack(player, item);
            if (stack != null) {
                cir.setReturnValue(new ItemStack[]{stack});
            }
        }
    }

    @Inject(method = "findAmmoStack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)[Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true)
    private static void tweaks$universalStacks(Player player, Item item, CallbackInfoReturnable<ItemStack[]> cir) {
        AmmoBridge.trackOfficial(player, cir.getReturnValue());
        ItemStack stack = AmmoBridge.universalStack(player, item);
        if (stack == null) {
            return;
        }
        ItemStack[] found = cir.getReturnValue();
        if (tweaks$empty(found)) {
            cir.setReturnValue(new ItemStack[]{stack});
            return;
        }
        ItemStack[] joined = Arrays.copyOf(found, found.length + 1);
        joined[found.length] = stack;
        cir.setReturnValue(joined);
    }

    @Inject(method = "getReserveAmmoCount(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)I",
            at = @At("HEAD"), cancellable = true)
    private static void tweaks$reserveInfinite(Player player, Item item, CallbackInfoReturnable<Integer> cir) {
        if (AmmoBridge.hasInfinite(player)) {
            cir.setReturnValue(Integer.MAX_VALUE);
        }
    }

    @Inject(method = "getReserveAmmoCount(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Ltop/ribs/scguns/common/Gun;)I",
            at = @At("RETURN"), cancellable = true)
    private static void tweaks$reserveTotal(Player player, ItemStack gunStack, Gun gun, CallbackInfoReturnable<Integer> cir) {
        if (AmmoBridge.hasInfinite(player)) {
            cir.setReturnValue(Integer.MAX_VALUE);
            return;
        }
        int universal = AmmoBridge.universalCount(player);
        if (universal > 0) {
            long total = (long) cir.getReturnValue() + universal;
            cir.setReturnValue((int) Math.min(Integer.MAX_VALUE, total));
        }
    }
}
