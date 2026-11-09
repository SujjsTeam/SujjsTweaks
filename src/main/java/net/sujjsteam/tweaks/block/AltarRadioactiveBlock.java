package net.sujjsteam.tweaks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.sujjsteam.tweaks.block.entity.AltarRadioactiveBlockEntity;
import net.sujjsteam.tweaks.entity.Nucleon;
import net.sujjsteam.tweaks.registry.ModBlockEntities;
import net.sujjsteam.tweaks.registry.ModItems;
import net.sujjsteam.tweaks.world.BossRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AltarRadioactiveBlock extends BaseEntityBlock {

    public static final MapCodec<AltarRadioactiveBlock> CODEC = simpleCodec(AltarRadioactiveBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public AltarRadioactiveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarRadioactiveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ALTAR_RADIOACTIVE.get(), AltarRadioactiveBlockEntity::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModItems.CRYSTAL_OF_FIRE.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof AltarRadioactiveBlockEntity altar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (altar.isRitualActive()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.sujjstweaks.altar_busy"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level instanceof ServerLevel serverLevel) {
            Optional<Double> dist = BossRegistry.get(serverLevel)
                    .distanceToNearest(serverLevel, pos, Nucleon.SINGLE_BOSS_RADIUS);
            if (dist.isPresent()) {
                player.displayClientMessage(
                        Component.translatable("message.sujjstweaks.boss_exists", (int) Math.round(dist.get())), true);
                serverLevel.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.5F);
                return ItemInteractionResult.CONSUME;
            }
            altar.startRitual(stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
