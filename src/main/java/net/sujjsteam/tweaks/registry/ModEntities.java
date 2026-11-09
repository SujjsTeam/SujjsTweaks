package net.sujjsteam.tweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.entity.IrradiatedSoldier;
import net.sujjsteam.tweaks.entity.Nucleon;
import net.sujjsteam.tweaks.entity.RadioactiveOrb;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Tweaks.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Nucleon>> NUCLEON = ENTITY_TYPES.register(
            "nucleon",
            () -> EntityType.Builder.of(Nucleon::new, MobCategory.MONSTER)
                    .sized(1.8F, 4.2F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("nucleon")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<RadioactiveOrb>> RADIOACTIVE_ORB = ENTITY_TYPES.register(
            "radioactive_orb",
            () -> EntityType.Builder.<RadioactiveOrb>of(RadioactiveOrb::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("radioactive_orb")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<IrradiatedSoldier>> IRRADIATED_SOLDIER = ENTITY_TYPES.register(
            "irradiated_soldier",
            () -> EntityType.Builder.of(IrradiatedSoldier::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.1F)
                    .clientTrackingRange(8)
                    .build("irradiated_soldier")
    );
}
