package net.sujjsteam.tweaks.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.sujjsteam.tweaks.client.RitualSkyClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class RitualSkyMixin {

    @Unique
    private static final Vec3 SUJJSTWEAKS_RITUAL_SKY = new Vec3(0.12, 0.62, 0.10);

    @Unique
    private static final Vec3 SUJJSTWEAKS_RITUAL_FLASH = new Vec3(0.65, 1.0, 0.5);

    @Unique
    private static final Vec3 SUJJSTWEAKS_RITUAL_CLOUDS = new Vec3(0.25, 0.75, 0.20);

    @Inject(method = "getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void sujjstweaks$ritualSkyColor(Vec3 position, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        float strength = RitualSkyClient.strength();
        float flash = RitualSkyClient.flash();
        if (strength <= 0.0F && flash <= 0.0F) {
            return;
        }
        Vec3 color = cir.getReturnValue().lerp(SUJJSTWEAKS_RITUAL_SKY, strength);
        cir.setReturnValue(color.lerp(SUJJSTWEAKS_RITUAL_FLASH, flash));
    }

    @Inject(method = "getCloudColor(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void sujjstweaks$ritualCloudColor(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        float strength = RitualSkyClient.strength();
        if (strength > 0.0F) {
            cir.setReturnValue(cir.getReturnValue().lerp(SUJJSTWEAKS_RITUAL_CLOUDS, strength));
        }
    }

    @Inject(method = "getStarBrightness(F)F", at = @At("RETURN"), cancellable = true)
    private void sujjstweaks$ritualStars(float partialTick, CallbackInfoReturnable<Float> cir) {
        float strength = RitualSkyClient.strength();
        if (strength > 0.0F) {
            cir.setReturnValue(cir.getReturnValue() * (1.0F - strength));
        }
    }
}
