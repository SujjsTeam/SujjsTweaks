package net.sujjsteam.tweaks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.sujjsteam.tweaks.controller.ControllerEffects;
import net.sujjsteam.tweaks.event.AtomicStrikes;
import net.sujjsteam.tweaks.registry.ModSounds;

public class AtomicBombItem extends Item {

    public static final double AIM_RANGE = 80.0;
    public static final int COOLDOWN = 40;

    public AtomicBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 eye = player.getEyePosition();
            Vec3 end = eye.add(player.getLookAngle().scale(AIM_RANGE));
            BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            Vec3 target = hit.getLocation();
            if (hit.getType() == HitResult.Type.MISS) {
                BlockPos ground = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(end));
                target = new Vec3(end.x, ground.getY(), end.z);
            }
            AtomicStrikes.start(serverLevel, target, player);
            serverLevel.playSound(null, target.x, target.y, target.z, ModSounds.NUCLEON_CHARGE_WINDUP.get(), SoundSource.PLAYERS, 6.0F, 0.7F);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.RITUAL_CLIMAX.get(), SoundSource.PLAYERS, 2.0F, 1.2F);
            serverLevel.sendParticles(ControllerEffects.dust(0xFF2010, 2.5F), target.x, target.y + 6.0, target.z, 120, 0.4, 6.0, 0.4, 0.0);
            player.displayClientMessage(Component.translatable("message.sujjstweaks.atomic_bomb_launched").withStyle(ChatFormatting.RED), true);
            player.awardStat(Stats.ITEM_USED.get(this));
            player.getCooldowns().addCooldown(this, COOLDOWN);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
