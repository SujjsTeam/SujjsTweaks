package net.sujjsteam.tweaks.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.registry.ModAttachments;
import net.sujjsteam.tweaks.ritual.RitualWeather;
import net.sujjsteam.tweaks.world.RadioactiveDimension;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public class RadioactiveDimensionEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !RadioactiveDimension.DIMENSION.equals(player.level().dimension())) {
            return;
        }
        if (!player.getData(ModAttachments.RADIATION_STABILIZED)) {
            MobEffectInstance nausea = player.getEffect(MobEffects.CONFUSION);
            if (nausea == null || nausea.getDuration() < 60) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, true, false, true));
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && !RitualWeather.ownsWeather(level)) {
            RitualWeather.tick(level);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (RadioactiveDimension.DIMENSION.equals(event.getFrom()) && event.getEntity() instanceof ServerPlayer player) {
            player.removeEffect(MobEffects.CONFUSION);
        }
    }

    public static void stabilize(ServerPlayer player) {
        player.setData(ModAttachments.RADIATION_STABILIZED, true);
        player.removeEffect(MobEffects.CONFUSION);
    }
}
