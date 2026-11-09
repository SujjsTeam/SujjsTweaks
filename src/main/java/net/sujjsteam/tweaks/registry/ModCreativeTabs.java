package net.sujjsteam.tweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.compat.scguns.ScorchedGunsCompat;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Tweaks.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TWEAKS_TAB = TABS.register(
            "sujjstweaks_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sujjstweaks"))
                    .icon(() -> ModItems.THE_ABSOLUTE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ATOMIC_BOMB.get());
                        output.accept(ModItems.CRYSTAL_OF_FIRE.get());
                        output.accept(ModItems.ALTAR_RADIOACTIVE.get());
                        output.accept(ModItems.NUCLEON_SPAWN_EGG.get());
                        output.accept(ModItems.IRRADIATED_SOLDIER_SPAWN_EGG.get());
                        output.accept(ModItems.RADIOACTIVE_FLASK.get());
                        output.accept(ModItems.DIMENSIONAL_CONTROLLER.get());
                        output.accept(ModItems.UNIVERSAL_AMMO.get());
                        output.accept(ModItems.INFINITE_AMMO.get());
                        output.accept(ModItems.RADIOACTIVE_ROUND.get());
                        output.accept(ModItems.RADIOACTIVE_GRASS_BLOCK.get());
                        output.accept(ModItems.RADIOACTIVE_LOG.get());
                        output.accept(ModItems.RADIOACTIVE_PLANKS.get());
                        output.accept(ModItems.RADIOACTIVE_LEAVES.get());
                        output.accept(ModItems.RADIOACTIVE_SAPLING.get());
                        if (ModList.get().isLoaded("scguns")) {
                            ScorchedGunsCompat.addToCreativeTab(output);
                        }
                        output.accept(ModItems.ABSOLUTE_FRAGMENT.get());
                        output.accept(ModItems.THE_ABSOLUTE.get());
                        output.accept(ModItems.THE_ABSOLUTE_BLOCK.get());
                    })
                    .build()
    );
}
