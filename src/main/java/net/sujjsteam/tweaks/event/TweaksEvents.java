package net.sujjsteam.tweaks.event;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.entity.IrradiatedSoldier;
import net.sujjsteam.tweaks.entity.Nucleon;
import net.sujjsteam.tweaks.registry.ModEntities;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public class TweaksEvents {

    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NUCLEON.get(), Nucleon.createAttributes().build());
        event.put(ModEntities.IRRADIATED_SOLDIER.get(), IrradiatedSoldier.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.IRRADIATED_SOLDIER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
