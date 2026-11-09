package net.sujjsteam.tweaks.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.compat.scguns.AmmoBridge;
import net.sujjsteam.tweaks.registry.ModEffects;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public class RadioactiveMinigunEvents {

    public static final ResourceLocation MINIGUN_ID = ResourceLocation.fromNamespaceAndPath(Tweaks.MOD_ID, "radioactive_minigun");
    public static final int EFFECT_DURATION = 100;
    public static final int EFFECT_AMPLIFIER = 1;

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player shooter) || source.getDirectEntity() == null || source.getDirectEntity() == shooter) {
            return;
        }
        ItemStack gun = shooter.getMainHandItem();
        if (!MINIGUN_ID.equals(BuiltInRegistries.ITEM.getKey(gun.getItem()))) {
            return;
        }
        if (AmmoBridge.hasInfinite(shooter) || AmmoBridge.isSpecialLoaded(gun)) {
            return;
        }
        event.getEntity().addEffect(new MobEffectInstance(ModEffects.RADIOACTIVE, EFFECT_DURATION, EFFECT_AMPLIFIER), shooter);
    }
}
