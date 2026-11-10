package net.sujjsteam.tweaks.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.item.AtomicBombItem;
import net.sujjsteam.tweaks.item.DimensionalControllerItem;
import net.sujjsteam.tweaks.item.InfiniteAmmoItem;
import net.sujjsteam.tweaks.item.TheAbsoluteItem;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Tweaks.MOD_ID);

    public static final DeferredItem<AtomicBombItem> ATOMIC_BOMB = ITEMS.registerItem(
            "atomic_bomb",
            AtomicBombItem::new,
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> CRYSTAL_OF_FIRE = ITEMS.registerItem(
            "crystal_of_fire",
            Item::new,
            new Item.Properties().stacksTo(16).fireResistant()
    );

    public static final DeferredItem<BlockItem> ALTAR_RADIOACTIVE = ITEMS.registerSimpleBlockItem(
            "altar_radioactive",
            ModBlocks.ALTAR_RADIOACTIVE
    );

    public static final DeferredItem<BlockItem> THE_ABSOLUTE_BLOCK = ITEMS.registerSimpleBlockItem(
            "the_absolute_block",
            ModBlocks.THE_ABSOLUTE_BLOCK,
            new Item.Properties().rarity(Rarity.EPIC).fireResistant()
    );

    public static final DeferredItem<DeferredSpawnEggItem> NUCLEON_SPAWN_EGG = ITEMS.register(
            "nucleon_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.NUCLEON, 0x1A221A, 0x5CFF3C, new Item.Properties())
    );

    public static final DeferredItem<DeferredSpawnEggItem> IRRADIATED_SOLDIER_SPAWN_EGG = ITEMS.register(
            "irradiated_soldier_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.IRRADIATED_SOLDIER, 0x4A5240, 0x5CFF3C, new Item.Properties())
    );

    public static final DeferredItem<Item> RADIOACTIVE_FLASK = ITEMS.registerItem(
            "radioactive_flask",
            Item::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.RARE).fireResistant()
    );

    public static final DeferredItem<Item> RADIOACTIVE_ORB = ITEMS.registerSimpleItem("radioactive_orb");

    public static final DeferredItem<DimensionalControllerItem> DIMENSIONAL_CONTROLLER = ITEMS.registerItem(
            "dimensional_controller",
            DimensionalControllerItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<Item> UNIVERSAL_AMMO = ITEMS.registerItem(
            "universal_ammo",
            Item::new,
            new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<BlockItem> RADIOACTIVE_GRASS_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.RADIOACTIVE_GRASS_BLOCK);
    public static final DeferredItem<BlockItem> RADIOACTIVE_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.RADIOACTIVE_LOG);
    public static final DeferredItem<BlockItem> RADIOACTIVE_PLANKS = ITEMS.registerSimpleBlockItem(ModBlocks.RADIOACTIVE_PLANKS);
    public static final DeferredItem<BlockItem> RADIOACTIVE_LEAVES = ITEMS.registerSimpleBlockItem(ModBlocks.RADIOACTIVE_LEAVES);
    public static final DeferredItem<BlockItem> RADIOACTIVE_SAPLING = ITEMS.registerSimpleBlockItem(ModBlocks.RADIOACTIVE_SAPLING);

    public static final DeferredItem<Item> RADIOACTIVE_ROUND = ITEMS.registerItem(
            "radioactive_round",
            Item::new,
            new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<InfiniteAmmoItem> INFINITE_AMMO = ITEMS.registerItem(
            "infinite_ammo",
            InfiniteAmmoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()
    );

    public static final DeferredItem<Item> ABSOLUTE_FRAGMENT = ITEMS.registerItem(
            "absolute_fragment",
            Item::new,
            new Item.Properties().stacksTo(64).rarity(Rarity.RARE).fireResistant()
    );

    public static final DeferredItem<TheAbsoluteItem> THE_ABSOLUTE = ITEMS.registerItem(
            "the_absolute",
            TheAbsoluteItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    );
}
