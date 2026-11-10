package net.sujjsteam.tweaks.compat.scguns;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.registry.ModSounds;

public final class ScorchedGunsCompat {

    public static final String MODEL_NAME = "sujjstweaks_radioactive_minigun";
    public static final int MINIGUN_DURABILITY = 4000;

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Tweaks.MOD_ID);

    public static final DeferredItem<RadioactiveMinigunItem> RADIOACTIVE_MINIGUN = ITEMS.register("radioactive_minigun",
            () -> new RadioactiveMinigunItem(
                    new Item.Properties().stacksTo(1).durability(MINIGUN_DURABILITY).rarity(Rarity.EPIC).fireResistant(),
                    MODEL_NAME,
                    top.ribs.scguns.init.ModSounds.MAG_OUT.get(),
                    top.ribs.scguns.init.ModSounds.MAG_IN.get(),
                    top.ribs.scguns.init.ModSounds.RELOAD_END.get(),
                    top.ribs.scguns.init.ModSounds.COPPER_GUN_JAM.get(),
                    top.ribs.scguns.init.ModSounds.COPPER_GUN_JAM.get()));

    private ScorchedGunsCompat() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void addToCreativeTab(CreativeModeTab.Output output) {
        output.accept(RADIOACTIVE_MINIGUN.get());
    }
}
