package net.sujjsteam.tweaks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;

public class RadioactiveGrassBlock extends SpreadingSnowyDirtBlock {

    public static final MapCodec<RadioactiveGrassBlock> CODEC = simpleCodec(RadioactiveGrassBlock::new);

    public RadioactiveGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SpreadingSnowyDirtBlock> codec() {
        return CODEC;
    }
}
