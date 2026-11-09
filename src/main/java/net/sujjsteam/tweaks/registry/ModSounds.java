package net.sujjsteam.tweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Tweaks.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_AMBIENT = register("nucleon_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_HURT = register("nucleon_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_DEATH = register("nucleon_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_ROAR = register("nucleon_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_EMERGE = register("nucleon_emerge");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_SLAM_SWING = register("nucleon_slam_swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_SLAM_IMPACT = register("nucleon_slam_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_CHARGE_WINDUP = register("nucleon_charge_windup");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_CHARGE_DASH = register("nucleon_charge_dash");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_CHARGE_CRASH = register("nucleon_charge_crash");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_SHOCKWAVE = register("nucleon_shockwave");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_CAST = register("nucleon_cast");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_ORB_SHOOT = register("nucleon_orb_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_ORB_HIT = register("nucleon_orb_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_SHIELD_BREAK = register("nucleon_shield_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_BARRIER_BLOCK = register("nucleon_barrier_block");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_BARRIER_UP = register("nucleon_barrier_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_BARRIER_DOWN = register("nucleon_barrier_down");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_EXPLODE = register("nucleon_explode");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_AMBIENT = register("soldier_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_HURT = register("soldier_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_DEATH = register("soldier_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_START = register("ritual_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_PULSE = register("ritual_pulse");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_CLIMAX = register("ritual_climax");
    public static final DeferredHolder<SoundEvent, SoundEvent> RITUAL_RELEASE = register("ritual_release");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEON_BATTLE_MUSIC = register("nucleon_battle_music");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Tweaks.MOD_ID, name)));
    }
}
