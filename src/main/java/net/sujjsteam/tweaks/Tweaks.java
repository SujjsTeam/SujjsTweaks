package net.sujjsteam.tweaks;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.sujjsteam.tweaks.compat.scguns.ScorchedGunsCompat;
import net.sujjsteam.tweaks.registry.ModAttachments;
import net.sujjsteam.tweaks.registry.ModBlockEntities;
import net.sujjsteam.tweaks.registry.ModBlocks;
import net.sujjsteam.tweaks.registry.ModCreativeTabs;
import net.sujjsteam.tweaks.registry.ModEffects;
import net.sujjsteam.tweaks.registry.ModEntities;
import net.sujjsteam.tweaks.registry.ModFeatures;
import net.sujjsteam.tweaks.registry.ModItems;
import net.sujjsteam.tweaks.registry.ModSounds;

@Mod(Tweaks.MOD_ID)
public class Tweaks {

    public static final String MOD_ID = "sujjstweaks";

    public Tweaks(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        if (ModList.get().isLoaded("scguns")) {
            ScorchedGunsCompat.register(modEventBus);
        }
        ModCreativeTabs.TABS.register(modEventBus);
        modEventBus.addListener(Tweaks::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FireBlock fire = (FireBlock) Blocks.FIRE;
            fire.setFlammable(ModBlocks.RADIOACTIVE_LOG.get(), 5, 5);
            fire.setFlammable(ModBlocks.RADIOACTIVE_PLANKS.get(), 5, 20);
            fire.setFlammable(ModBlocks.RADIOACTIVE_LEAVES.get(), 30, 60);
        });
    }
}
