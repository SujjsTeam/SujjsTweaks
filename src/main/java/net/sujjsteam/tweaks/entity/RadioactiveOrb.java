package net.sujjsteam.tweaks.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.sujjsteam.tweaks.registry.ModSounds;
import org.joml.Vector3f;

public class RadioactiveOrb extends Fireball {

    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.4F, 1.0F, 0.3F), 1.6F);

    public RadioactiveOrb(EntityType<? extends RadioactiveOrb> type, Level level) {
        super(type, level);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return TRAIL;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(target instanceof Nucleon) && !(target instanceof IrradiatedSoldier);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(level() instanceof ServerLevel)) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = getOwner();
        float damage = owner instanceof Nucleon boss ? boss.orbDamage() : 10.0F;
        if (target.hurt(damageSources().indirectMagic(this, owner), damage) && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), owner);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(TRAIL, getX(), getY(), getZ(), 30, 0.5, 0.5, 0.5, 0.0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 10, 0.3, 0.3, 0.3, 0.3);
            playSound(ModSounds.NUCLEON_ORB_HIT.get(), 2.0F, 0.9F + random.nextFloat() * 0.2F);
            discard();
        }
    }
}
