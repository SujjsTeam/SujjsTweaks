package net.sujjsteam.tweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.block.entity.AltarRadioactiveBlockEntity;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Tweaks.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarRadioactiveBlockEntity>> ALTAR_RADIOACTIVE =
            BLOCK_ENTITY_TYPES.register(
                    "altar_radioactive",
                    () -> BlockEntityType.Builder
                            .of(AltarRadioactiveBlockEntity::new, ModBlocks.ALTAR_RADIOACTIVE.get())
                            .build(null)
            );
}
