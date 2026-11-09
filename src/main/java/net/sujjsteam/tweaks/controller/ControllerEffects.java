package net.sujjsteam.tweaks.controller;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.sujjsteam.tweaks.registry.ModSounds;
import org.joml.Vector3f;

public final class ControllerEffects {

    private ControllerEffects() {
    }

    public static DustParticleOptions dust(int color, float size) {
        return new DustParticleOptions(new Vector3f(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F), size);
    }

    public static void start(ServerLevel level, ServerPlayer player, ControllerProfile profile) {
        level.playSound(null, player.blockPosition(), ModSounds.RITUAL_START.get(), SoundSource.PLAYERS, 1.5F, 1.2F);
        Vec3 c = player.position();
        level.sendParticles(dust(profile.color(), 1.4F), c.x, c.y + 1.0, c.z, 30, 0.6, 0.8, 0.6, 0.0);
    }

    public static void charging(ServerLevel level, ServerPlayer player, ControllerProfile profile, int ticks, float progress) {
        Vec3 c = player.position();
        DustParticleOptions primary = dust(profile.color(), 1.1F + progress * 0.6F);
        DustParticleOptions secondary = dust(profile.secondaryColor(), 1.0F + progress * 0.5F);
        double outer = 1.8 - 0.9 * progress;
        double lowY = 0.15 + progress * 1.0 + 0.2 * Math.sin(ticks * 0.2);
        for (int i = 0; i < 16; i++) {
            double a = i * (Math.PI * 2.0 / 16.0) + ticks * 0.18;
            level.sendParticles(primary, c.x + Math.cos(a) * outer, c.y + lowY, c.z + Math.sin(a) * outer, 1, 0.0, 0.0, 0.0, 0.0);
        }
        double inner = 1.2 - 0.5 * progress;
        double highY = 1.9 - progress * 0.6;
        for (int i = 0; i < 12; i++) {
            double a = i * (Math.PI * 2.0 / 12.0) - ticks * 0.24;
            level.sendParticles(secondary, c.x + Math.cos(a) * inner, c.y + highY, c.z + Math.sin(a) * inner, 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (int arm = 0; arm < 3; arm++) {
            double a = ticks * 0.35 + arm * (Math.PI * 2.0 / 3.0);
            double h = (ticks * 0.08 + arm * 0.8) % 2.4;
            double r = 1.0 - 0.3 * progress;
            level.sendParticles(arm % 2 == 0 ? primary : secondary, c.x + Math.cos(a) * r, c.y + h, c.z + Math.sin(a) * r, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.PORTAL, c.x, c.y + 1.0, c.z, 2 + (int) (10 * progress), 1.2, 1.0, 1.2, 0.6);
        if (progress > 0.5F && ticks % 4 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 1.8, c.z, 3, 0.5, 0.4, 0.5, 0.15);
        }
        if (progress > 0.85F) {
            level.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 0.5, c.z, 2, 0.4, 0.2, 0.4, 0.05);
        }
        if (ticks % 20 == 0) {
            level.playSound(null, player.blockPosition(), ModSounds.RITUAL_PULSE.get(), SoundSource.PLAYERS, 1.2F, 0.8F + progress * 0.8F);
        }
        if (profile.chargeTime() > 60 && ticks == profile.chargeTime() - 40) {
            level.playSound(null, player.blockPosition(), ModSounds.RITUAL_CLIMAX.get(), SoundSource.PLAYERS, 1.8F, 1.1F);
        }
    }

    public static void complete(ServerLevel level, ServerPlayer player, ControllerProfile profile) {
        Vec3 c = player.position();
        level.sendParticles(ParticleTypes.FLASH, c.x, c.y + 1.0, c.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, 80, 0.2, 0.4, 0.2, 0.35);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y + 1.0, c.z, 100, 0.3, 0.6, 0.3, 0.6);
        for (int ring = 1; ring <= 4; ring++) {
            DustParticleOptions color = dust(ring % 2 == 0 ? profile.secondaryColor() : profile.color(), 1.8F);
            int points = 20 + ring * 10;
            for (int i = 0; i < points; i++) {
                double a = i * (Math.PI * 2.0 / points);
                level.sendParticles(color, c.x + Math.cos(a) * ring, c.y + 0.2, c.z + Math.sin(a) * ring, 1, 0.0, 0.05, 0.0, 0.0);
            }
        }
        level.playSound(null, player.blockPosition(), ModSounds.RITUAL_RELEASE.get(), SoundSource.PLAYERS, 2.0F, 1.15F);
    }

    public static void fail(ServerLevel level, ServerPlayer player, ControllerProfile profile) {
        Vec3 c = player.position();
        DustParticleOptions red = dust(0xFF3020, 1.6F);
        for (int i = 0; i < 24; i++) {
            double a = i * (Math.PI * 2.0 / 24.0);
            level.sendParticles(red, c.x + Math.cos(a) * 0.6, c.y + 1.0, c.z + Math.sin(a) * 0.6, 1, Math.cos(a) * 0.1, 0.0, Math.sin(a) * 0.1, 0.0);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 1.0, c.z, 25, 0.4, 0.6, 0.4, 0.03);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 1.2, c.z, 20, 0.5, 0.5, 0.5, 0.3);
        level.sendParticles(dust(profile.color(), 1.2F), c.x, c.y + 1.0, c.z, 30, 0.9, 0.9, 0.9, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 0.6F);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    public static void burnOut(ServerLevel level, ServerPlayer player) {
        Vec3 c = player.position();
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 1.0, c.z, 2, 0.3, 0.3, 0.3, 0.0);
        level.sendParticles(ParticleTypes.LAVA, c.x, c.y + 1.0, c.z, 10, 0.3, 0.3, 0.3, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 1.2, c.z, 40, 0.4, 0.8, 0.4, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.5F, 0.7F);
        level.playSound(null, player.blockPosition(), ModSounds.NUCLEON_ORB_HIT.get(), SoundSource.PLAYERS, 1.5F, 1.3F);
    }

    public static void cancel(ServerLevel level, ServerPlayer player, ControllerProfile profile) {
        Vec3 c = player.position();
        level.sendParticles(ParticleTypes.SMOKE, c.x, c.y + 1.0, c.z, 20, 0.4, 0.5, 0.4, 0.02);
        level.sendParticles(dust(profile.color(), 1.0F), c.x, c.y + 1.0, c.z, 15, 0.6, 0.6, 0.6, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
    }
}
