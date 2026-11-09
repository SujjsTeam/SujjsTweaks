package net.sujjsteam.tweaks.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.sujjsteam.tweaks.entity.Nucleon;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NucleonCombatGoal extends Goal {

    private enum Attack { NONE, SLAM, CHARGE_WINDUP, CHARGE_DASH, STOMP, CAST }

    private static final double SLAM_REACH = 4.5;
    private static final int SLAM_IMPACT = 11;
    private static final int SLAM_DURATION = 20;
    private static final int CHARGE_WINDUP = 20;
    private static final int CHARGE_MAX_TICKS = 32;
    private static final double CHARGE_SPEED = 0.85;
    private static final int STOMP_IMPACT = 15;
    private static final int STOMP_DURATION = 26;
    private static final int CAST_FIRE = 12;
    private static final int CAST_SECOND_FIRE = 24;
    private static final int CAST_DURATION = 22;

    private final Nucleon boss;
    private final Set<UUID> chargeHits = new HashSet<>();
    private Attack attack = Attack.NONE;
    private int ticks;
    private int repathTicks;
    private Vec3 chargeDirection = Vec3.ZERO;
    private double chargeDistance;
    private double chargeTraveled;
    private int volleys;

    public NucleonCombatGoal(Nucleon boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = boss.getTarget();
        return target != null && target.isAlive() && !boss.isSpawning() && !boss.isStunned();
    }

    @Override
    public boolean canContinueToUse() {
        if (boss.isStunned()) {
            return false;
        }
        return canUse() || attack != Attack.NONE;
    }

    @Override
    public void stop() {
        if (attack == Attack.CHARGE_DASH) {
            boss.endDash();
        }
        attack = Attack.NONE;
        boss.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        switch (attack) {
            case NONE -> choose(target);
            case SLAM -> tickSlam(target);
            case CHARGE_WINDUP -> tickChargeWindup(target);
            case CHARGE_DASH -> tickChargeDash();
            case STOMP -> tickStomp(target);
            case CAST -> tickCast(target);
        }
    }

    private void begin(Attack next) {
        attack = next;
        ticks = 0;
        boss.getNavigation().stop();
    }

    private void choose(LivingEntity target) {
        if (target == null) {
            return;
        }
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distance = Math.sqrt(boss.distanceToSqr(target));
        boolean sees = boss.getSensing().hasLineOfSight(target);
        if (distance <= SLAM_REACH && boss.isSlamReady()) {
            begin(Attack.SLAM);
            boss.startSlam();
            return;
        }
        if (boss.phase() >= 2 && distance <= 12.0 && boss.isShockwaveReady() && boss.onGround()) {
            begin(Attack.STOMP);
            boss.startStomp();
            return;
        }
        if (boss.phase() >= 3 && distance >= 5.0 && distance <= 32.0 && sees && boss.isOrbReady()) {
            begin(Attack.CAST);
            volleys = boss.isRaging() ? 2 : 1;
            boss.startCast();
            return;
        }
        if (distance >= 7.0 && distance <= 22.0 && sees && boss.isChargeReady() && boss.onGround()) {
            begin(Attack.CHARGE_WINDUP);
            boss.startChargeWindup();
            return;
        }
        if (--repathTicks <= 0) {
            repathTicks = 10;
            boss.getNavigation().moveTo(target, 1.0);
        }
    }

    private void tickSlam(LivingEntity target) {
        ticks++;
        boss.getNavigation().stop();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (ticks == SLAM_IMPACT) {
            boss.slamImpact();
        }
        if (ticks >= SLAM_DURATION) {
            boss.resetSlamCooldown();
            attack = Attack.NONE;
        }
    }

    private void tickChargeWindup(LivingEntity target) {
        ticks++;
        boss.getNavigation().stop();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (ticks < CHARGE_WINDUP) {
            return;
        }
        if (target == null) {
            boss.resetChargeCooldown();
            attack = Attack.NONE;
            return;
        }
        Vec3 offset = new Vec3(target.getX() - boss.getX(), 0.0, target.getZ() - boss.getZ());
        double horizontal = offset.length();
        chargeDirection = horizontal > 0.001 ? offset.scale(1.0 / horizontal) : boss.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
        chargeDistance = Mth.clamp(horizontal + 4.0, 8.0, 26.0);
        chargeTraveled = 0.0;
        chargeHits.clear();
        boss.beginDash();
        attack = Attack.CHARGE_DASH;
        ticks = 0;
    }

    private void tickChargeDash() {
        ticks++;
        boss.getNavigation().stop();
        float yaw = (float) (Mth.atan2(chargeDirection.z, chargeDirection.x) * (180.0 / Math.PI)) - 90.0F;
        boss.setYRot(yaw);
        boss.yBodyRot = yaw;
        boss.yHeadRot = yaw;
        boss.setDeltaMovement(chargeDirection.x * CHARGE_SPEED, boss.getDeltaMovement().y, chargeDirection.z * CHARGE_SPEED);
        chargeTraveled += CHARGE_SPEED;
        boss.dashTick(chargeHits);
        if (boss.horizontalCollision && ticks > 3) {
            boss.crash();
            attack = Attack.NONE;
            return;
        }
        if (chargeTraveled >= chargeDistance || ticks >= CHARGE_MAX_TICKS) {
            boss.endDash();
            boss.resetChargeCooldown();
            attack = Attack.NONE;
        }
    }

    private void tickStomp(LivingEntity target) {
        ticks++;
        boss.getNavigation().stop();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (ticks == STOMP_IMPACT) {
            boss.stompImpact();
        }
        if (ticks >= STOMP_DURATION) {
            boss.resetShockwaveCooldown();
            attack = Attack.NONE;
        }
    }

    private void tickCast(LivingEntity target) {
        ticks++;
        boss.getNavigation().stop();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (target != null && (ticks == CAST_FIRE || (volleys > 1 && ticks == CAST_SECOND_FIRE))) {
            boss.fireOrbs(target);
        }
        if (ticks >= CAST_DURATION + (volleys > 1 ? 12 : 0)) {
            boss.resetOrbCooldown();
            attack = Attack.NONE;
        }
    }
}
