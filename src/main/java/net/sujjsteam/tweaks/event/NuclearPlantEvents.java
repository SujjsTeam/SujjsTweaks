package net.sujjsteam.tweaks.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.sujjsteam.tweaks.Tweaks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public class NuclearPlantEvents {

    public static final ResourceKey<Structure> NUCLEAR_POWER_PLANT =
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(Tweaks.MOD_ID, "nuclear_power_plant"));
    public static final int CHECK_INTERVAL = 10;
    public static final int FATIGUE_AMPLIFIER = 2;
    public static final int FATIGUE_DURATION = 40;
    public static final boolean PROTECT_FROM_EXPLOSIONS = true;

    private static final Set<UUID> INSIDE = new HashSet<>();

    public static boolean isInside(ServerLevel level, BlockPos pos) {
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(NUCLEAR_POWER_PLANT);
        if (structure == null) {
            return false;
        }
        return level.structureManager().getStructureWithPieceAt(pos, structure).isValid();
    }

    public static boolean isInside(ServerPlayer player) {
        return isInside(player.serverLevel(), player.blockPosition());
    }

    private static boolean hasPlantFatigue(Player player) {
        MobEffectInstance current = player.getEffect(MobEffects.DIG_SLOWDOWN);
        return current != null && current.isAmbient() && current.getAmplifier() == FATIGUE_AMPLIFIER
                && current.getDuration() <= FATIGUE_DURATION;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.isCreative()) {
            return;
        }
        Level level = player.level();
        boolean protectedBlock = level instanceof ServerLevel serverLevel
                ? event.getPosition().map(pos -> isInside(serverLevel, pos)).orElse(false)
                : hasPlantFatigue(player);
        if (protectedBlock) {
            event.setNewSpeed(0.0F);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().isCreative() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (isInside(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (PROTECT_FROM_EXPLOSIONS && event.getLevel() instanceof ServerLevel level) {
            event.getAffectedBlocks().removeIf(pos -> isInside(level, pos));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        if (isInside(player)) {
            INSIDE.add(player.getUUID());
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, FATIGUE_DURATION, FATIGUE_AMPLIFIER, true, true, true));
        } else if (INSIDE.remove(player.getUUID())) {
            if (hasPlantFatigue(player)) {
                player.removeEffect(MobEffects.DIG_SLOWDOWN);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        INSIDE.remove(event.getEntity().getUUID());
    }
}
