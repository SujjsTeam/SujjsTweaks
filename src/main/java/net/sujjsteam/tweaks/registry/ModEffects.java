package net.sujjsteam.tweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.effect.RadioactiveEffect;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Tweaks.MOD_ID);

    public static final DeferredHolder<MobEffect, RadioactiveEffect> RADIOACTIVE = MOB_EFFECTS.register("radioactive", RadioactiveEffect::new);
}
