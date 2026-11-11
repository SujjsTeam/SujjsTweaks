package net.sujjsteam.tweaks.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.registry.ModEntities;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public final class SujjsMobDrops {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        EntityType<?> type = event.getEntity().getType();
        if (type != ModEntities.NUCLEON.get() && type != ModEntities.IRRADIATED_SOLDIER.get()) {
            return;
        }
        event.getDrops().removeIf(SujjsMobDrops::isForeignDrop);
    }

    private static boolean isForeignDrop(ItemEntity drop) {
        String namespace = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).getNamespace();
        return !namespace.equals(Tweaks.MOD_ID);
    }

    private SujjsMobDrops() {
    }
}
