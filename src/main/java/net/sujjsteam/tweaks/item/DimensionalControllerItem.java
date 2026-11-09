package net.sujjsteam.tweaks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.sujjsteam.tweaks.controller.ControllerEffects;
import net.sujjsteam.tweaks.controller.ControllerProfile;
import net.sujjsteam.tweaks.controller.event.DimensionalControllerCancelEvent;
import net.sujjsteam.tweaks.controller.event.DimensionalControllerCompleteEvent;
import net.sujjsteam.tweaks.controller.event.DimensionalControllerStartEvent;
import net.sujjsteam.tweaks.event.RadioactiveDimensionEvents;
import net.sujjsteam.tweaks.registry.ModAttachments;
import net.sujjsteam.tweaks.world.RadioactiveDimension;

public class DimensionalControllerItem extends Item {

    public static final String ACTIVE = "ControllerActive";
    public static final String CHARGE = "ControllerCharge";
    public static final String COLOR = "ControllerColor";
    public static final String SECONDARY_COLOR = "ControllerColor2";
    public static final String FAILURES = "ControllerFailures";

    public DimensionalControllerItem(Properties properties) {
        super(properties);
    }

    public static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean isActive(ItemStack stack) {
        return data(stack).getBoolean(ACTIVE);
    }

    public static int failures(ItemStack stack) {
        return data(stack).getInt(FAILURES);
    }

    private static void setFailures(ItemStack stack, int failures) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (failures > 0) {
                tag.putInt(FAILURES, failures);
            } else {
                tag.remove(FAILURES);
            }
        });
        if (data(stack).isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    private static void activate(ItemStack stack, ControllerProfile profile) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ACTIVE, true);
            tag.putInt(CHARGE, profile.chargeTime());
            tag.putInt(COLOR, profile.color());
            tag.putInt(SECONDARY_COLOR, profile.secondaryColor());
        });
    }

    private static void deactivate(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(ACTIVE);
            tag.remove(CHARGE);
            tag.remove(COLOR);
            tag.remove(SECONDARY_COLOR);
        });
        if (data(stack).isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ControllerProfile profile = RadioactiveDimension.controllerProfile(serverLevel.dimension());
            if (profile == null) {
                serverPlayer.displayClientMessage(Component.translatable("message.sujjstweaks.controller_no_signal"), true);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.8F);
                return InteractionResultHolder.fail(stack);
            }
            if (NeoForge.EVENT_BUS.post(new DimensionalControllerStartEvent(serverPlayer, profile)).isCanceled()) {
                return InteractionResultHolder.fail(stack);
            }
            activate(stack, profile);
            ControllerEffects.start(serverLevel, serverPlayer, profile);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }
        ControllerProfile profile = RadioactiveDimension.controllerProfile(serverLevel.dimension());
        if (profile == null || !isActive(stack)) {
            deactivate(stack);
            player.stopUsingItem();
            return;
        }
        int ticks = player.getTicksUsingItem();
        float progress = Math.min(1.0F, ticks / (float) profile.chargeTime());
        ControllerEffects.charging(serverLevel, player, profile, ticks, progress);
        if (ticks >= profile.chargeTime()) {
            deactivate(stack);
            player.stopUsingItem();
            if (profile.cooldown() > 0) {
                player.getCooldowns().addCooldown(this, profile.cooldown());
            }
            if (!RadioactiveDimension.DIMENSION.equals(profile.dimension())) {
                attemptJump(serverLevel, player, stack, profile);
                return;
            }
            ControllerEffects.complete(serverLevel, player, profile);
            if (player.getData(ModAttachments.RADIATION_STABILIZED)) {
                NeoForge.EVENT_BUS.post(new DimensionalControllerCompleteEvent(player, profile));
                returnToOverworld(serverLevel, player, profile);
                return;
            }
            RadioactiveDimensionEvents.stabilize(player);
            player.displayClientMessage(Component.translatable("message.sujjstweaks.radiation_stabilized"), true);
            NeoForge.EVENT_BUS.post(new DimensionalControllerCompleteEvent(player, profile));
        }
    }

    private void attemptJump(ServerLevel serverLevel, ServerPlayer player, ItemStack stack, ControllerProfile profile) {
        boolean creative = player.getAbilities().instabuild;
        if (player.getRandom().nextFloat() < RadioactiveDimension.JUMP_CHANCE) {
            ControllerEffects.complete(serverLevel, player, profile);
            setFailures(stack, 0);
            player.displayClientMessage(Component.translatable("message.sujjstweaks.controller_jump_success"), true);
            NeoForge.EVENT_BUS.post(new DimensionalControllerCompleteEvent(player, profile));
            ServerLevel radioactiveLevel = serverLevel.getServer().getLevel(RadioactiveDimension.DIMENSION);
            if (radioactiveLevel != null) {
                player.setData(ModAttachments.DIMENSION_RETURN_POS, player.position());
                player.setData(ModAttachments.DIMENSION_RETURN_ROT, new Vec3(player.getYRot(), player.getXRot(), 0.0));
                BlockPos column = BlockPos.containing(player.getX(), 0, player.getZ());
                radioactiveLevel.getChunkAt(column);
                int surface = radioactiveLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
                player.teleportTo(radioactiveLevel, player.getX(), surface + 0.1, player.getZ(), player.getYRot(), player.getXRot());
                player.resetFallDistance();
            }
            return;
        }
        ControllerEffects.fail(serverLevel, player, profile);
        if (creative) {
            player.displayClientMessage(Component.translatable("message.sujjstweaks.controller_jump_failed",
                    RadioactiveDimension.MAX_JUMP_ATTEMPTS - failures(stack)), true);
            return;
        }
        int failed = failures(stack) + 1;
        if (failed >= RadioactiveDimension.MAX_JUMP_ATTEMPTS) {
            ControllerEffects.burnOut(serverLevel, player);
            player.displayClientMessage(Component.translatable("message.sujjstweaks.controller_broken").withStyle(ChatFormatting.RED), true);
            stack.shrink(1);
            return;
        }
        setFailures(stack, failed);
        player.displayClientMessage(Component.translatable("message.sujjstweaks.controller_jump_failed",
                RadioactiveDimension.MAX_JUMP_ATTEMPTS - failed), true);
    }

    private void returnToOverworld(ServerLevel radioactiveLevel, ServerPlayer player, ControllerProfile profile) {
        ServerLevel overworld = radioactiveLevel.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        Vec3 target;
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        if (player.hasData(ModAttachments.DIMENSION_RETURN_POS)) {
            target = player.getData(ModAttachments.DIMENSION_RETURN_POS);
            Vec3 rot = player.getData(ModAttachments.DIMENSION_RETURN_ROT);
            yRot = (float) rot.x;
            xRot = (float) rot.y;
        } else {
            BlockPos bed = Level.OVERWORLD.equals(player.getRespawnDimension()) ? player.getRespawnPosition() : null;
            BlockPos base = bed != null ? bed.above() : overworld.getSharedSpawnPos();
            target = Vec3.atBottomCenterOf(base);
        }
        BlockPos feet = BlockPos.containing(target);
        boolean blocked = !overworld.getBlockState(feet).getCollisionShape(overworld, feet).isEmpty()
                || !overworld.getBlockState(feet.above()).getCollisionShape(overworld, feet.above()).isEmpty();
        if (blocked) {
            BlockPos top = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, feet);
            target = new Vec3(target.x, top.getY(), target.z);
        }
        player.removeData(ModAttachments.DIMENSION_RETURN_POS);
        player.removeData(ModAttachments.DIMENSION_RETURN_ROT);
        player.displayClientMessage(Component.translatable("message.sujjstweaks.controller_return"), true);
        player.teleportTo(overworld, target.x, target.y, target.z, yRot, xRot);
        player.resetFallDistance();
        ControllerEffects.complete(overworld, player, profile);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return failures(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int left = RadioactiveDimension.MAX_JUMP_ATTEMPTS - failures(stack);
        return Math.round(13.0F * left / RadioactiveDimension.MAX_JUMP_ATTEMPTS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float left = (RadioactiveDimension.MAX_JUMP_ATTEMPTS - failures(stack)) / (float) RadioactiveDimension.MAX_JUMP_ATTEMPTS;
        return Mth.hsvToRgb(Math.max(0.0F, left) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player) || !isActive(stack)) {
            return;
        }
        deactivate(stack);
        ControllerProfile profile = RadioactiveDimension.controllerProfile(serverLevel.dimension());
        if (profile == null) {
            return;
        }
        ControllerEffects.cancel(serverLevel, player, profile);
        NeoForge.EVENT_BUS.post(new DimensionalControllerCancelEvent(player, profile));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof LivingEntity living && isActive(stack)
                && !(living.isUsingItem() && living.getUseItem() == stack)) {
            deactivate(stack);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}
