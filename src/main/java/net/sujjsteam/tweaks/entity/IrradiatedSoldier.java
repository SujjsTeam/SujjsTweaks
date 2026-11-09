package net.sujjsteam.tweaks.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.sujjsteam.tweaks.registry.ModSounds;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class IrradiatedSoldier extends Monster implements GeoEntity {

    public static final int SPAWN_DURATION = 30;

    private static final EntityDataAccessor<Integer> SPAWN_TIMER =
            SynchedEntityData.defineId(IrradiatedSoldier.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.irradiated_soldier.idle");
    private static final RawAnimation WALKING = RawAnimation.begin().thenLoop("animation.irradiated_soldier.walk");
    private static final RawAnimation EMERGING = RawAnimation.begin().thenPlay("animation.irradiated_soldier.spawn");
    private static final RawAnimation DYING = RawAnimation.begin().thenPlayAndHold("animation.irradiated_soldier.death");
    private static final RawAnimation THRUST = RawAnimation.begin().thenPlay("animation.irradiated_soldier.attack");

    private static final DustParticleOptions GREEN_DUST =
            new DustParticleOptions(new Vector3f(0.4F, 1.0F, 0.3F), 1.5F);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID master;

    public IrradiatedSoldier(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    public void setMaster(Nucleon boss) {
        this.master = boss.getUUID();
    }

    public void empower(int wave) {
        int extra = Math.max(0, wave - 1);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0 + 10.0 * extra);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.0 + 1.0 * extra);
        getAttribute(Attributes.ARMOR).setBaseValue(10.0 + 2.0 * extra);
        setHealth(getMaxHealth());
    }

    public boolean isSpawning() {
        return entityData.get(SPAWN_TIMER) > 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPAWN_TIMER, SPAWN_DURATION);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, Nucleon.class, IrradiatedSoldier.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int spawnTicks = entityData.get(SPAWN_TIMER);
        if (spawnTicks > 0) {
            entityData.set(SPAWN_TIMER, spawnTicks - 1);
            getNavigation().stop();
        }
        if (master != null && tickCount > 100 && tickCount % 20 == 0 && level() instanceof ServerLevel serverLevel) {
            if (!(serverLevel.getEntity(master) instanceof Nucleon boss && boss.isAlive())) {
                serverLevel.sendParticles(GREEN_DUST, getX(), getY() + 1.0, getZ(), 30, 0.4, 0.8, 0.4, 0.0);
                kill();
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (isSpawning()) {
            return false;
        }
        triggerAnim("actions", "thrust");
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isSpawning() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof Nucleon || other instanceof IrradiatedSoldier || super.isAlliedTo(other);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GREEN_DUST, getX(), getY() + 1.0, getZ(), 20, 0.4, 0.8, 0.4, 0.0);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 10, 0.3, 0.6, 0.3, 0.02);
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return ModSounds.SOLDIER_AMBIENT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SOLDIER_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return ModSounds.SOLDIER_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpawnTicks", entityData.get(SPAWN_TIMER));
        if (master != null) {
            tag.putUUID("Master", master);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SpawnTicks")) {
            entityData.set(SPAWN_TIMER, tag.getInt("SpawnTicks"));
        }
        if (tag.hasUUID("Master")) {
            master = tag.getUUID("Master");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::animateMovement));
        controllers.add(new AnimationController<>(this, "actions", 0, state -> PlayState.STOP)
                .triggerableAnim("thrust", THRUST));
    }

    private PlayState animateMovement(AnimationState<IrradiatedSoldier> state) {
        if (isDeadOrDying()) {
            return state.setAndContinue(DYING);
        }
        if (isSpawning()) {
            return state.setAndContinue(EMERGING);
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
