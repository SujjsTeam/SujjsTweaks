package net.sujjsteam.tweaks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.sujjsteam.tweaks.entity.ai.NucleonCombatGoal;
import net.sujjsteam.tweaks.registry.ModEntities;
import net.sujjsteam.tweaks.registry.ModItems;
import net.sujjsteam.tweaks.registry.ModSounds;
import net.sujjsteam.tweaks.ritual.RitualSky;
import net.sujjsteam.tweaks.ritual.RitualWeather;
import net.sujjsteam.tweaks.world.BossRegistry;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Nucleon extends Monster implements GeoEntity {

    public static final double SINGLE_BOSS_RADIUS = 5000.0;
    public static final int SPAWN_DURATION = 60;
    public static final float SPAWN_SKY_STRENGTH = 1.0F;
    public static final int DEATH_DURATION = 60;
    private static final float MAX_DAMAGE_PER_HIT = 30.0F;
    public static final float[] SHIELD_THRESHOLDS = {0.75F, 0.5F, 0.25F};
    private static final int[] SOLDIERS_PER_SHIELD = {4, 8, 12};
    private static final int SOLDIERS_PER_RING = 6;
    public static final int SHIELD_BREAK_DURATION = 40;
    private static final int SUMMON_DELAY = 30;
    private static final int BARRIER_TIME_LIMIT = 2400;
    private static final int BARRIER_EXTRA_TIME_PER_WAVE = 1200;

    private static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(Nucleon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPAWN_TIMER =
            SynchedEntityData.defineId(Nucleon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DASHING =
            SynchedEntityData.defineId(Nucleon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STUN_TIMER =
            SynchedEntityData.defineId(Nucleon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PROTECTED =
            SynchedEntityData.defineId(Nucleon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SHIELD_BREAK_TIMER =
            SynchedEntityData.defineId(Nucleon.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.nucleon.idle");
    private static final RawAnimation WALKING = RawAnimation.begin().thenLoop("animation.nucleon.walk");
    private static final RawAnimation EMERGING = RawAnimation.begin().thenPlay("animation.nucleon.spawn");
    private static final RawAnimation DYING = RawAnimation.begin().thenPlayAndHold("animation.nucleon.death");
    private static final RawAnimation SLAM = RawAnimation.begin().thenPlay("animation.nucleon.attack");
    private static final RawAnimation ROAR = RawAnimation.begin().thenPlay("animation.nucleon.roar");
    private static final RawAnimation SHIELD_BREAK = RawAnimation.begin().thenPlay("animation.nucleon.shield_break");
    private static final RawAnimation CHARGE_WINDUP = RawAnimation.begin().thenPlay("animation.nucleon.charge_windup");
    private static final RawAnimation CHARGING = RawAnimation.begin().thenLoop("animation.nucleon.charge");
    private static final RawAnimation STOMP = RawAnimation.begin().thenPlay("animation.nucleon.stomp");
    private static final RawAnimation CAST = RawAnimation.begin().thenPlay("animation.nucleon.cast");
    private static final RawAnimation STUNNED = RawAnimation.begin().thenLoop("animation.nucleon.stunned");
    private static final int STUN_DURATION = 40;
    private static final double SHOCKWAVE_SPEED = 0.6;
    private static final double SHOCKWAVE_MAX_RADIUS = 14.0;

    private static final DustParticleOptions GREEN_DUST =
            new DustParticleOptions(new Vector3f(0.4F, 1.0F, 0.3F), 2.0F);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar = new ServerBossEvent(
            Component.translatable("entity.sujjstweaks.nucleon"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.NOTCHED_10);

    private int invulnerableTicks;
    private int slamCooldownTicks;
    private int chargeCooldownTicks = 100;
    private int shockwaveCooldownTicks = 60;
    private int orbCooldownTicks = 60;
    private final List<Shockwave> shockwaves = new ArrayList<>();

    private static final class Shockwave {
        final Vec3 center;
        final Set<UUID> hit = new HashSet<>();
        double radius;

        Shockwave(Vec3 center, double radius) {
            this.center = center;
            this.radius = radius;
        }
    }
    private int pendingSummon;
    private int currentWave;
    private int barrierTicks;
    private final List<UUID> soldiers = new ArrayList<>();
    private final Map<UUID, Integer> lastWarning = new HashMap<>();

    public Nucleon(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        bossBar.setDarkenScreen(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800.0)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ARMOR, 18.0)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, 1);
        builder.define(SPAWN_TIMER, SPAWN_DURATION);
        builder.define(PROTECTED, false);
        builder.define(DASHING, false);
        builder.define(STUN_TIMER, 0);
        builder.define(SHIELD_BREAK_TIMER, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new NucleonCombatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public int phase() {
        return entityData.get(PHASE);
    }

    public int remainingShields() {
        return Math.max(0, SHIELD_THRESHOLDS.length + 1 - phase());
    }

    public int slamCooldown() {
        return 25 - 4 * (phase() - 1);
    }

    public boolean isRaging() {
        return remainingShields() == 0 && !isProtected();
    }

    public boolean isDashing() {
        return entityData.get(DASHING);
    }

    public boolean isStunned() {
        return entityData.get(STUN_TIMER) > 0;
    }

    public boolean isSlamReady() {
        return slamCooldownTicks <= 0;
    }

    public boolean isChargeReady() {
        return chargeCooldownTicks <= 0;
    }

    public boolean isShockwaveReady() {
        return shockwaveCooldownTicks <= 0;
    }

    public boolean isOrbReady() {
        return orbCooldownTicks <= 0;
    }

    public void resetSlamCooldown() {
        slamCooldownTicks = slamCooldown();
    }

    public void resetChargeCooldown() {
        chargeCooldownTicks = 170 - 20 * (phase() - 1);
    }

    public void resetShockwaveCooldown() {
        shockwaveCooldownTicks = 190 - 30 * Math.max(0, phase() - 2);
    }

    public void resetOrbCooldown() {
        orbCooldownTicks = 130 - 25 * Math.max(0, phase() - 3);
    }

    public float orbDamage() {
        return 10.0F * (1.0F + 0.1F * (phase() - 1));
    }

    private float attackMultiplier() {
        return 1.0F + 0.15F * (phase() - 1);
    }

    private BossEvent.BossBarColor barColor() {
        return switch (remainingShields()) {
            case 3 -> BossEvent.BossBarColor.GREEN;
            case 2 -> BossEvent.BossBarColor.YELLOW;
            case 1 -> BossEvent.BossBarColor.RED;
            default -> BossEvent.BossBarColor.PURPLE;
        };
    }

    public boolean isSpawning() {
        return entityData.get(SPAWN_TIMER) > 0;
    }

    public boolean isProtected() {
        return entityData.get(PROTECTED);
    }

    public int shieldBreakTicks() {
        return entityData.get(SHIELD_BREAK_TIMER);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && isSpawning()) {
            RitualSky.report(getX(), getY(), getZ(), SPAWN_SKY_STRENGTH, 0.0F);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int spawnTicks = entityData.get(SPAWN_TIMER);
        if (spawnTicks > 0 && level() instanceof ServerLevel stormLevel && !RitualWeather.ownsWeather(stormLevel)) {
            RitualWeather.force(stormLevel, 1.0F);
        }
        if (spawnTicks > 0) {
            entityData.set(SPAWN_TIMER, spawnTicks - 1);
            getNavigation().stop();
            if (spawnTicks == SPAWN_DURATION) {
                playSound(ModSounds.NUCLEON_EMERGE.get(), 4.0F, 1.0F);
            }
            if (spawnTicks == 1) {
                roar();
            }
        }
        if (invulnerableTicks > 0) {
            invulnerableTicks--;
        }
        int cooldownStep = isRaging() ? 2 : 1;
        slamCooldownTicks -= cooldownStep;
        chargeCooldownTicks -= cooldownStep;
        shockwaveCooldownTicks -= cooldownStep;
        orbCooldownTicks -= cooldownStep;
        int stun = entityData.get(STUN_TIMER);
        if (stun > 0) {
            entityData.set(STUN_TIMER, stun - 1);
            getNavigation().stop();
            if (stun % 6 == 0 && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 4.0, getZ(), 6, 0.6, 0.2, 0.6, 0.1);
            }
            if (stun == 1) {
                resetChargeCooldown();
            }
        }
        if (!shockwaves.isEmpty() && level() instanceof ServerLevel serverLevel) {
            tickShockwaves(serverLevel);
        }
        int breakTicks = entityData.get(SHIELD_BREAK_TIMER);
        if (breakTicks > 0) {
            entityData.set(SHIELD_BREAK_TIMER, breakTicks - 1);
        }
        if (pendingSummon > 0 && --pendingSummon == 0) {
            roar();
            summonSoldiers();
        }
        if (isProtected()) {
            barrierTicks++;
            if (tickCount % 10 == 0) {
                checkSoldiers();
            }
        }
        checkShields();
        bossBar.setProgress(getHealth() / getMaxHealth());
        if (tickCount % 20 == 0 && level() instanceof ServerLevel serverLevel) {
            BossRegistry.get(serverLevel).update(getUUID(), blockPosition());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (reason.shouldDestroy() && level() instanceof ServerLevel serverLevel) {
            BossRegistry.get(serverLevel).unregister(getUUID());
        }
    }

    private void checkShields() {
        int broken = phase() - 1;
        if (broken >= SHIELD_THRESHOLDS.length || isProtected() || pendingSummon > 0) {
            return;
        }
        float healthRatio = getHealth() / getMaxHealth();
        if (healthRatio <= SHIELD_THRESHOLDS[broken] + 0.0001F) {
            entityData.set(PHASE, broken + 2);
            bossBar.setColor(barColor());
            breakShield(broken + 1);
        }
    }

    private void roar() {
        triggerAnim("actions", "roar");
        playSound(ModSounds.NUCLEON_ROAR.get(), 5.0F, 0.95F + random.nextFloat() * 0.1F);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 2.5, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            for (int i = 0; i < 48; i++) {
                double a = i * (Math.PI * 2.0 / 48.0);
                serverLevel.sendParticles(GREEN_DUST, getX() + Math.cos(a) * 3.0, getY() + 0.2, getZ() + Math.sin(a) * 3.0,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            AABB area = getBoundingBox().inflate(10.0, 4.0, 10.0);
            for (Player playerEntity : serverLevel.getEntitiesOfClass(Player.class, area)) {
                double dx = getX() - playerEntity.getX();
                double dz = getZ() - playerEntity.getZ();
                playerEntity.knockback(1.5, dx, dz);
                playerEntity.hurtMarked = true;
            }
        }
    }

    private void breakShield(int wave) {
        currentWave = wave;
        entityData.set(SHIELD_BREAK_TIMER, SHIELD_BREAK_DURATION);
        invulnerableTicks = SUMMON_DELAY + 20;
        pendingSummon = SUMMON_DELAY;
        triggerAnim("actions", "shield_break");
        playSound(ModSounds.NUCLEON_SHIELD_BREAK.get(), 5.0F, 1.0F);
        if (level() instanceof ServerLevel serverLevel) {
            double cy = getY() + 2.1;
            BlockParticleOption shards = new BlockParticleOption(ParticleTypes.BLOCK, switch (wave) {
                case 1 -> Blocks.LIME_STAINED_GLASS.defaultBlockState();
                case 2 -> Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
                default -> Blocks.RED_STAINED_GLASS.defaultBlockState();
            });
            serverLevel.sendParticles(shards, getX(), cy, getZ(), 220, 1.6, 1.6, 1.6, 0.6);
            serverLevel.sendParticles(ParticleTypes.FLASH, getX(), cy, getZ(), 2, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), cy, getZ(), 80, 0.3, 0.3, 0.3, 0.45);
            serverLevel.sendParticles(GREEN_DUST, getX(), cy, getZ(), 120, 2.4, 2.4, 2.4, 0.0);
            for (Player playerEntity : serverLevel.getEntitiesOfClass(Player.class, getBoundingBox().inflate(48.0))) {
                playerEntity.displayClientMessage(Component.translatable("message.sujjstweaks.shield_broken", wave, SHIELD_THRESHOLDS.length), true);
            }
        }
    }

    private void checkSoldiers() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        soldiers.removeIf(id -> !(serverLevel.getEntity(id) instanceof IrradiatedSoldier soldier && soldier.isAlive()));
        if (soldiers.isEmpty() || barrierTicks > BARRIER_TIME_LIMIT + BARRIER_EXTRA_TIME_PER_WAVE * (currentWave - 1)) {
            dropBarrier(serverLevel);
        }
    }

    private void dropBarrier(ServerLevel serverLevel) {
        entityData.set(PROTECTED, false);
        barrierTicks = 0;
        double cy = getY() + 2.1;
        BlockParticleOption shards = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LIME_STAINED_GLASS.defaultBlockState());
        serverLevel.sendParticles(shards, getX(), cy, getZ(), 90, 1.3, 1.3, 1.3, 0.4);
        serverLevel.sendParticles(GREEN_DUST, getX(), cy, getZ(), 60, 1.8, 1.8, 1.8, 0.0);
        playSound(ModSounds.NUCLEON_BARRIER_DOWN.get(), 4.0F, 1.0F);
        boolean raging = remainingShields() == 0;
        if (raging) {
            applyRage();
        }
        for (Player playerEntity : serverLevel.getEntitiesOfClass(Player.class, getBoundingBox().inflate(48.0))) {
            playerEntity.displayClientMessage(Component.translatable(raging ? "message.sujjstweaks.enraged" : "message.sujjstweaks.barrier_down"), true);
        }
    }

    private void applyRage() {
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.34);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(24.0);
        playSound(ModSounds.NUCLEON_ROAR.get(), 5.0F, 0.8F);
        triggerAnim("actions", "roar");
    }

    private void blockHit(DamageSource source) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity attacker = source.getEntity();
        Vec3 center = position().add(0.0, 2.1, 0.0);
        Vec3 point = center;
        if (attacker != null) {
            Vec3 direction = attacker.getEyePosition().subtract(center);
            if (direction.lengthSqr() > 0.0001) {
                point = center.add(direction.normalize().scale(2.3));
            }
        }
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 12, 0.2, 0.2, 0.2, 0.25);
        serverLevel.sendParticles(GREEN_DUST, point.x, point.y, point.z, 8, 0.2, 0.2, 0.2, 0.0);
        playSound(ModSounds.NUCLEON_BARRIER_BLOCK.get(), 2.0F, 0.9F + random.nextFloat() * 0.2F);
        if (attacker instanceof Player playerEntity) {
            int now = tickCount;
            Integer before = lastWarning.get(playerEntity.getUUID());
            if (before == null || now - before > 40) {
                lastWarning.put(playerEntity.getUUID(), now);
                playerEntity.displayClientMessage(Component.translatable("message.sujjstweaks.kill_soldiers"), true);
            }
        }
    }

    private void summonSoldiers() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 3.0F, 0.6F);
        int wave = Math.max(1, Math.min(currentWave, SOLDIERS_PER_SHIELD.length));
        int total = SOLDIERS_PER_SHIELD[wave - 1];
        for (int i = 0; i < total; i++) {
            int ring = i / SOLDIERS_PER_RING;
            int inRing = Math.min(SOLDIERS_PER_RING, total - ring * SOLDIERS_PER_RING);
            double angle = (i % SOLDIERS_PER_RING) * (Math.PI * 2.0 / inRing) + ring * 0.5 + random.nextDouble() * 0.3;
            double radius = 4.0 + ring * 3.0;
            BlockPos spot = BlockPos.containing(getX() + Math.cos(angle) * radius, getY(), getZ() + Math.sin(angle) * radius);
            for (int raise = 0; raise < 4 && !serverLevel.getBlockState(spot).getCollisionShape(serverLevel, spot).isEmpty(); raise++) {
                spot = spot.above();
            }
            IrradiatedSoldier soldier = ModEntities.IRRADIATED_SOLDIER.get().create(serverLevel);
            if (soldier == null) {
                continue;
            }
            soldier.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            soldier.setMaster(this);
            soldier.empower(wave);
            if (getTarget() != null) {
                soldier.setTarget(getTarget());
            }
            serverLevel.addFreshEntity(soldier);
            soldiers.add(soldier.getUUID());
            serverLevel.sendParticles(GREEN_DUST, spot.getX() + 0.5, spot.getY() + 0.2, spot.getZ() + 0.5, 40, 0.5, 0.2, 0.5, 0.0);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, spot.getX() + 0.5, spot.getY() + 0.3, spot.getZ() + 0.5, 12, 0.4, 0.2, 0.4, 0.02);
            serverLevel.playSound(null, spot, SoundEvents.WARDEN_DIG, getSoundSource(), 1.5F, 1.0F);
        }
        if (!soldiers.isEmpty()) {
            entityData.set(PROTECTED, true);
            barrierTicks = 0;
            playSound(ModSounds.NUCLEON_BARRIER_UP.get(), 4.0F, 1.0F);
        }
        for (Player playerEntity : serverLevel.getEntitiesOfClass(Player.class, getBoundingBox().inflate(48.0))) {
            playerEntity.displayClientMessage(Component.translatable("message.sujjstweaks.soldiers_called"), true);
        }
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof IrradiatedSoldier || super.isAlliedTo(other);
    }

    public void startSlam() {
        triggerAnim("actions", "slam");
        playSound(ModSounds.NUCLEON_SLAM_SWING.get(), 3.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    public void slamImpact() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 look = getLookAngle();
        Vec3 front = position().add(look.x * 2.2, 0.0, look.z * 2.2);
        AABB area = new AABB(front.x - 2.5, front.y - 1.0, front.z - 2.5, front.x + 2.5, front.y + 3.0, front.z + 2.5);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * attackMultiplier();
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != this && e.isAlive() && !isAlliedTo(e))) {
            if (target.hurt(damageSources().mobAttack(this), damage)) {
                target.knockback(1.6, getX() - target.getX(), getZ() - target.getZ());
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), this);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.45, 0.0));
                target.hurtMarked = true;
            }
        }
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, front.x, front.y + 0.2, front.z, 2, 0.6, 0.1, 0.6, 0.0);
        for (int i = 0; i < 32; i++) {
            double a = i * (Math.PI * 2.0 / 32.0);
            serverLevel.sendParticles(GREEN_DUST, front.x + Math.cos(a) * 2.2, front.y + 0.15, front.z + Math.sin(a) * 2.2,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        playSound(ModSounds.NUCLEON_SLAM_IMPACT.get(), 4.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if ((isSpawning() || invulnerableTicks > 0) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (isProtected() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            blockHit(source);
            return false;
        }
        float finalAmount = Math.min(amount * (isStunned() ? 1.5F : 1.0F), MAX_DAMAGE_PER_HIT);
        int broken = phase() - 1;
        if (broken < SHIELD_THRESHOLDS.length && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            float limit = SHIELD_THRESHOLDS[broken] * getMaxHealth();
            if (getHealth() - finalAmount < limit) {
                finalAmount = Math.max(0.0F, getHealth() - limit);
            }
            if (finalAmount <= 0.0F) {
                return false;
            }
        }
        return super.hurt(source, finalAmount);
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (level() instanceof ServerLevel serverLevel) {
            if (deathTime % 4 == 0) {
                serverLevel.sendParticles(GREEN_DUST, getX(), getY() + 1.8, getZ(), 25, 1.0, 1.5, 1.0, 0.0);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 2.0, getZ(), 6, 0.8, 1.2, 0.8, 0.2);
            }
            if (deathTime >= DEATH_DURATION && !isRemoved()) {
                serverLevel.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.5, getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.5, getZ(), 2, 1.0, 1.0, 1.0, 0.0);
                serverLevel.sendParticles(GREEN_DUST, getX(), getY() + 1.5, getZ(), 150, 2.0, 2.0, 2.0, 0.0);
                playSound(ModSounds.NUCLEON_EXPLODE.get(), 6.0F, 1.0F);
                serverLevel.broadcastEntityEvent(this, (byte) 60);
                remove(RemovalReason.KILLED);
            }
        }
    }

    public void startChargeWindup() {
        triggerAnim("actions", "charge_windup");
        playSound(ModSounds.NUCLEON_CHARGE_WINDUP.get(), 4.0F, 1.0F);
    }

    public void beginDash() {
        entityData.set(DASHING, true);
        playSound(ModSounds.NUCLEON_CHARGE_DASH.get(), 4.0F, 1.0F);
    }

    public void endDash() {
        entityData.set(DASHING, false);
    }

    public void dashTick(Set<UUID> alreadyHit) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState ground = serverLevel.getBlockState(blockPosition().below());
        if (!ground.isAir()) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 0.1, getZ(),
                    8, 0.8, 0.1, 0.8, 0.1);
        }
        serverLevel.sendParticles(GREEN_DUST, getX(), getY() + 1.5, getZ(), 4, 0.6, 1.0, 0.6, 0.0);
        float damage = 16.0F * attackMultiplier();
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.6),
                e -> e != this && e.isAlive() && !isAlliedTo(e) && !alreadyHit.contains(e.getUUID()))) {
            alreadyHit.add(target.getUUID());
            if (target.hurt(damageSources().mobAttack(this), damage)) {
                Vec3 push = getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
                target.knockback(2.5, -push.x, -push.z);
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.5, 0.0));
                target.hurtMarked = true;
                playSound(ModSounds.NUCLEON_SLAM_IMPACT.get(), 3.0F, 1.2F);
            }
        }
    }

    public void crash() {
        endDash();
        entityData.set(STUN_TIMER, STUN_DURATION);
        setDeltaMovement(Vec3.ZERO);
        playSound(ModSounds.NUCLEON_CHARGE_CRASH.get(), 5.0F, 1.0F);
        if (level() instanceof ServerLevel serverLevel) {
            Vec3 front = position().add(getLookAngle().multiply(1.5, 0.0, 1.5)).add(0.0, 1.5, 0.0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, front.x, front.y, front.z, 3, 0.8, 0.8, 0.8, 0.0);
            serverLevel.sendParticles(GREEN_DUST, front.x, front.y, front.z, 40, 1.0, 1.0, 1.0, 0.0);
        }
    }

    public void startStomp() {
        triggerAnim("actions", "stomp");
        playSound(ModSounds.NUCLEON_SLAM_SWING.get(), 3.0F, 0.8F);
    }

    public void stompImpact() {
        playSound(ModSounds.NUCLEON_SHOCKWAVE.get(), 5.0F, 1.0F);
        Vec3 center = position();
        shockwaves.add(new Shockwave(center, 1.0));
        if (isRaging()) {
            shockwaves.add(new Shockwave(center, -3.0));
        }
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.3, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(GREEN_DUST, getX(), getY() + 0.3, getZ(), 60, 1.5, 0.2, 1.5, 0.0);
        }
    }

    private void tickShockwaves(ServerLevel serverLevel) {
        float damage = 14.0F * attackMultiplier();
        Iterator<Shockwave> iterator = shockwaves.iterator();
        while (iterator.hasNext()) {
            Shockwave wave = iterator.next();
            wave.radius += SHOCKWAVE_SPEED;
            if (wave.radius > SHOCKWAVE_MAX_RADIUS) {
                iterator.remove();
                continue;
            }
            if (wave.radius <= 0.0) {
                continue;
            }
            int points = (int) (wave.radius * 7.0);
            for (int i = 0; i < points; i++) {
                double angle = i * (Math.PI * 2.0 / points);
                double x = wave.center.x + Math.cos(angle) * wave.radius;
                double z = wave.center.z + Math.sin(angle) * wave.radius;
                serverLevel.sendParticles(GREEN_DUST, x, wave.center.y + 0.2, z, 1, 0.0, 0.05, 0.0, 0.0);
                if (i % 3 == 0) {
                    BlockState ground = serverLevel.getBlockState(BlockPos.containing(x, wave.center.y - 0.5, z));
                    if (!ground.isAir()) {
                        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), x, wave.center.y + 0.3, z,
                                2, 0.1, 0.15, 0.1, 0.05);
                    }
                }
            }
            double reach = wave.radius + 1.0;
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(wave.center.x - reach, wave.center.y - 1.0, wave.center.z - reach,
                            wave.center.x + reach, wave.center.y + 2.0, wave.center.z + reach),
                    e -> e != this && e.isAlive() && !isAlliedTo(e) && !wave.hit.contains(e.getUUID()))) {
                double dx = target.getX() - wave.center.x;
                double dz = target.getZ() - wave.center.z;
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (Math.abs(distance - wave.radius) > 0.8 || !target.onGround() || Math.abs(target.getY() - wave.center.y) > 1.5) {
                    continue;
                }
                wave.hit.add(target.getUUID());
                if (target.hurt(damageSources().mobAttack(this), damage)) {
                    target.knockback(1.2, -dx, -dz);
                    target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.6, 0.0));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2), this);
                    target.hurtMarked = true;
                }
            }
        }
    }

    public void startCast() {
        triggerAnim("actions", "cast");
        playSound(ModSounds.NUCLEON_CAST.get(), 4.0F, 1.0F);
    }

    public void fireOrbs(LivingEntity target) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int count = isRaging() ? 5 : 3;
        Vec3 origin = position().add(0.0, 3.4, 0.0).add(getLookAngle().multiply(1.2, 0.0, 1.2));
        Vec3 aim = target.getEyePosition().subtract(origin);
        for (int i = 0; i < count; i++) {
            float spread = (float) Math.toRadians((i - (count - 1) / 2.0) * 9.0);
            Vec3 direction = aim.yRot(spread);
            RadioactiveOrb orb = ModEntities.RADIOACTIVE_ORB.get().create(serverLevel);
            if (orb == null) {
                continue;
            }
            orb.setOwner(this);
            orb.setItem(new ItemStack(ModItems.RADIOACTIVE_ORB.get()));
            orb.setPos(origin.x, origin.y, origin.z);
            orb.accelerationPower = 0.04;
            orb.shoot(direction.x, direction.y, direction.z, 0.8F, 0.0F);
            serverLevel.addFreshEntity(orb);
        }
        playSound(ModSounds.NUCLEON_ORB_SHOOT.get(), 4.0F, 0.95F + random.nextFloat() * 0.1F);
        serverLevel.sendParticles(GREEN_DUST, origin.x, origin.y, origin.z, 25, 0.4, 0.4, 0.4, 0.0);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        Player killer = damageSource.getEntity() instanceof Player player ? player : (recentlyHit ? lastHurtByPlayer : null);
        if (killer instanceof ServerPlayer && !(killer instanceof FakePlayer)) {
            spawnAtLocation(new ItemStack(ModItems.RADIOACTIVE_FLASK.get()));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.NUCLEON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.NUCLEON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.NUCLEON_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", phase());
        tag.putInt("Wave", currentWave);
        tag.putInt("SpawnTicks", entityData.get(SPAWN_TIMER));
        tag.putBoolean("Protected", isProtected());
        tag.putInt("BarrierTicks", barrierTicks);
        tag.putInt("PendingSummon", pendingSummon);
        ListTag list = new ListTag();
        for (UUID id : soldiers) {
            list.add(NbtUtils.createUUID(id));
        }
        tag.put("Soldiers", list);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Phase")) {
            entityData.set(PHASE, tag.getInt("Phase"));
            bossBar.setColor(barColor());
        }
        currentWave = tag.getInt("Wave");
        if (remainingShields() == 0 && !tag.getBoolean("Protected")) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.34);
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(24.0);
        }
        if (tag.contains("SpawnTicks")) {
            entityData.set(SPAWN_TIMER, tag.getInt("SpawnTicks"));
        }
        entityData.set(PROTECTED, tag.getBoolean("Protected"));
        barrierTicks = tag.getInt("BarrierTicks");
        pendingSummon = tag.getInt("PendingSummon");
        soldiers.clear();
        ListTag list = tag.getList("Soldiers", Tag.TAG_INT_ARRAY);
        for (Tag item : list) {
            soldiers.add(NbtUtils.loadUUID(item));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::animateMovement));
        controllers.add(new AnimationController<>(this, "actions", 0, state -> PlayState.STOP)
                .triggerableAnim("slam", SLAM)
                .triggerableAnim("roar", ROAR)
                .triggerableAnim("shield_break", SHIELD_BREAK)
                .triggerableAnim("charge_windup", CHARGE_WINDUP)
                .triggerableAnim("stomp", STOMP)
                .triggerableAnim("cast", CAST));
    }

    private PlayState animateMovement(AnimationState<Nucleon> state) {
        if (isDeadOrDying()) {
            return state.setAndContinue(DYING);
        }
        if (isSpawning()) {
            return state.setAndContinue(EMERGING);
        }
        if (isStunned()) {
            return state.setAndContinue(STUNNED);
        }
        if (isDashing()) {
            return state.setAndContinue(CHARGING);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALKING);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
