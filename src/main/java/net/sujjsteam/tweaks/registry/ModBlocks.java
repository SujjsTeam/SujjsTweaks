package net.sujjsteam.tweaks.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.block.AltarRadioactiveBlock;
import net.sujjsteam.tweaks.block.RadioactiveGrassBlock;
import net.sujjsteam.tweaks.block.TheAbsoluteBlock;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Tweaks.MOD_ID);

    public static final DeferredBlock<AltarRadioactiveBlock> ALTAR_RADIOACTIVE = BLOCKS.register(
            "altar_radioactive",
            () -> new AltarRadioactiveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(-1.0F, 3600000.0F)
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(AltarRadioactiveBlock.LIT) ? 15 : 7)
                    .sound(SoundType.NETHERITE_BLOCK))
    );

    public static final DeferredBlock<TheAbsoluteBlock> THE_ABSOLUTE_BLOCK = BLOCKS.register(
            "the_absolute_block",
            () -> new TheAbsoluteBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(TheAbsoluteBlock.HARDNESS, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> 15)
                    .sound(SoundType.AMETHYST))
    );

    public static final DeferredBlock<RadioactiveGrassBlock> RADIOACTIVE_GRASS_BLOCK = BLOCKS.register(
            "radioactive_grass_block",
            () -> new RadioactiveGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN))
    );

    public static final DeferredBlock<RotatedPillarBlock> RADIOACTIVE_LOG = BLOCKS.register(
            "radioactive_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).mapColor(MapColor.COLOR_GRAY))
    );

    public static final DeferredBlock<Block> RADIOACTIVE_PLANKS = BLOCKS.register(
            "radioactive_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(MapColor.COLOR_GRAY))
    );

    public static final DeferredBlock<LeavesBlock> RADIOACTIVE_LEAVES = BLOCKS.register(
            "radioactive_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.COLOR_LIGHT_GREEN).lightLevel(state -> 3))
    );

    public static final DeferredBlock<SaplingBlock> RADIOACTIVE_SAPLING = BLOCKS.register(
            "radioactive_sapling",
            () -> new SaplingBlock(ModFeatures.RADIOACTIVE_TREE_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).lightLevel(state -> 2))
    );
}
