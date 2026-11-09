package net.sujjsteam.tweaks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class TheAbsoluteBlock extends Block {

    public static final float HARDNESS = 3600.0F;
    public static final float BASE_SPEED = 9.0F;

    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.82F, 0.25F), 0.8F);
    private static final DustParticleOptions PURPLE = new DustParticleOptions(new Vector3f(0.68F, 0.32F, 0.95F), 0.8F);

    public TheAbsoluteBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        for (int i = 0; i < 2; i++) {
            Direction face = Direction.getRandom(random);
            double x = cx + (face.getStepX() != 0 ? face.getStepX() * 0.55 : random.nextDouble() - 0.5);
            double y = cy + (face.getStepY() != 0 ? face.getStepY() * 0.55 : random.nextDouble() - 0.5);
            double z = cz + (face.getStepZ() != 0 ? face.getStepZ() * 0.55 : random.nextDouble() - 0.5);
            level.addParticle(random.nextBoolean() ? GOLD : PURPLE, x, y, z, 0.0, 0.015, 0.0);
        }
        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.END_ROD, cx + (random.nextDouble() - 0.5) * 0.6, pos.getY() + 1.05,
                    cz + (random.nextDouble() - 0.5) * 0.6, 0.0, 0.04 + random.nextDouble() * 0.03, 0.0);
        }
        if (random.nextInt(40) == 0) {
            level.playLocalSound(cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                    0.6F, 0.6F + random.nextFloat() * 0.6F, false);
        }
    }
}
