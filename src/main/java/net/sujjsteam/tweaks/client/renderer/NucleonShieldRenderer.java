package net.sujjsteam.tweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.sujjsteam.tweaks.entity.IrradiatedSoldier;
import net.sujjsteam.tweaks.entity.Nucleon;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class NucleonShieldRenderer {

    private static final float CENTER_HEIGHT = 2.1F;
    private static final float SHIELD_RADIUS = 2.6F;
    private static final float BARRIER_RADIUS = 2.3F;

    private NucleonShieldRenderer() {
    }

    public static void draw(Nucleon boss, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        if (boss.isDeadOrDying() || boss.isSpawning()) {
            return;
        }
        float time = boss.tickCount + partialTick;
        VertexConsumer light = buffers.getBuffer(RenderType.lightning());
        poseStack.pushPose();
        poseStack.translate(0.0F, CENTER_HEIGHT, 0.0F);
        Matrix4f m = poseStack.last().pose();

        int remaining = boss.remainingShields();
        if (remaining > 0 && !boss.isProtected() && boss.shieldBreakTicks() == 0) {
            int index = Nucleon.SHIELD_THRESHOLDS.length - remaining;
            float upper = index == 0 ? 1.0F : Nucleon.SHIELD_THRESHOLDS[index - 1];
            float lower = Nucleon.SHIELD_THRESHOLDS[index];
            float healthRatio = boss.getHealth() / boss.getMaxHealth();
            float damage = Math.max(0.0F, Math.min(1.0F, (upper - healthRatio) / (upper - lower)));
            float[] color = shieldColor(remaining);
            float pulse = 0.5F + 0.5F * (float) Math.sin(time * 0.15F);
            float blink = damage > 0.7F && ((int) (time * 0.6F)) % 5 == 0 ? 0.5F : 1.0F;
            float hitFlash = boss.hurtTime > 0 ? boss.hurtTime / 10.0F : 0.0F;
            float alpha = (0.07F + 0.04F * pulse + 0.3F * hitFlash) * blink;
            sphere(light, m, SHIELD_RADIUS, color[0], color[1], color[2], alpha, time * 0.6F);
            cracks(light, m, SHIELD_RADIUS + 0.01F, damage, boss.getId() + remaining * 101, (0.5F + 0.4F * hitFlash) * blink);
            for (int k = 0; k < remaining; k++) {
                double angle = time * 0.05 + k * (Math.PI * 2.0 / remaining);
                Vector3f center = new Vector3f((float) Math.cos(angle) * 1.1F, 2.0F, (float) Math.sin(angle) * 1.1F);
                beam(light, m, new Vector3f(center).add(0.0F, -0.18F, 0.0F), new Vector3f(center).add(0.0F, 0.18F, 0.0F),
                        0.12F, color[0], color[1], color[2], 0.8F);
            }
        }

        int breakTicks = boss.shieldBreakTicks();
        if (breakTicks > 0) {
            float t = 1.0F - (breakTicks - partialTick) / Nucleon.SHIELD_BREAK_DURATION;
            float radius = SHIELD_RADIUS * (1.0F + t * 1.4F);
            float alpha = 0.45F * (1.0F - t);
            float[] brokenColor = shieldColor(remaining + 1);
            sphere(light, m, radius, brokenColor[0], brokenColor[1], brokenColor[2], alpha, time * 2.0F);
            cracks(light, m, radius + 0.01F, 1.0F, boss.getId(), alpha * 1.6F);
        }

        if (boss.isProtected()) {
            float pulse = 0.5F + 0.5F * (float) Math.sin(time * 0.3F);
            float hitFlash = boss.hurtTime > 0 ? 0.3F : 0.0F;
            sphere(light, m, BARRIER_RADIUS, 0.5F, 1.0F, 0.35F, 0.1F + 0.06F * pulse + hitFlash, -time * 1.2F);
            Vec3 bossPos = boss.getPosition(partialTick);
            for (IrradiatedSoldier soldier : boss.level().getEntitiesOfClass(
                    IrradiatedSoldier.class, boss.getBoundingBox().inflate(32.0), IrradiatedSoldier::isAlive)) {
                Vec3 rel = soldier.getPosition(partialTick).add(0.0, 1.4, 0.0).subtract(bossPos);
                Vector3f end = new Vector3f((float) rel.x, (float) rel.y - CENTER_HEIGHT, (float) rel.z);
                float wave = 0.5F + 0.5F * (float) Math.sin(time * 0.5F + soldier.getId());
                beam(light, m, new Vector3f(0.0F, 0.0F, 0.0F), end, 0.09F, 0.4F, 1.0F, 0.3F, 0.35F + 0.25F * wave);
                beam(light, m, new Vector3f(0.0F, 0.0F, 0.0F), end, 0.03F, 1.0F, 1.0F, 1.0F, 0.6F);
            }
        }
        poseStack.popPose();
    }

    private static float[] shieldColor(int remaining) {
        return switch (remaining) {
            case 3 -> new float[]{0.35F, 1.0F, 0.3F};
            case 2 -> new float[]{1.0F, 0.85F, 0.25F};
            default -> new float[]{1.0F, 0.32F, 0.2F};
        };
    }

    private static void sphere(VertexConsumer light, Matrix4f m, float radius, float r, float g, float b, float alpha, float spin) {
        if (alpha <= 0.005F) {
            return;
        }
        int bands = 12;
        int slices = 28;
        double offset = Math.toRadians(spin);
        for (int i = 0; i < bands; i++) {
            double t0 = Math.PI * i / bands;
            double t1 = Math.PI * (i + 1) / bands;
            float bandBrightness = (i % 3 == 0) ? 1.6F : 1.0F;
            for (int j = 0; j < slices; j++) {
                double f0 = offset + Math.PI * 2.0 * j / slices;
                double f1 = offset + Math.PI * 2.0 * (j + 1) / slices;
                Vector3f a = point(radius, t0, f0);
                Vector3f bb = point(radius, t0, f1);
                Vector3f c = point(radius, t1, f1);
                Vector3f d = point(radius, t1, f0);
                quad(light, m, a, bb, c, d, r, g, b, Math.min(1.0F, alpha * bandBrightness));
            }
        }
    }

    private static void cracks(VertexConsumer light, Matrix4f m, float radius, float damage, int seed, float alpha) {
        int count = (int) (damage * 16.0F);
        if (count <= 0 || alpha <= 0.005F) {
            return;
        }
        RandomSource rng = RandomSource.create(seed * 31L + 7L);
        for (int k = 0; k < count; k++) {
            double theta = 0.35 + rng.nextDouble() * (Math.PI - 0.7);
            double phi = rng.nextDouble() * Math.PI * 2.0;
            Vector3f previous = point(radius, theta, phi);
            for (int s = 0; s < 5; s++) {
                theta += (rng.nextDouble() - 0.5) * 0.35;
                phi += (rng.nextDouble() - 0.5) * 0.35;
                Vector3f current = point(radius, theta, phi);
                beam(light, m, previous, current, 0.03F, 0.85F, 1.0F, 0.75F, alpha);
                previous = current;
            }
        }
    }

    private static Vector3f point(float radius, double theta, double phi) {
        return new Vector3f(
                (float) (radius * Math.sin(theta) * Math.cos(phi)),
                (float) (radius * Math.cos(theta)),
                (float) (radius * Math.sin(theta) * Math.sin(phi)));
    }

    private static void beam(VertexConsumer light, Matrix4f m, Vector3f from, Vector3f to, float width,
                              float r, float g, float b, float alpha) {
        Vector3f direction = new Vector3f(to).sub(from);
        if (direction.lengthSquared() < 0.000001F) {
            return;
        }
        direction.normalize();
        Vector3f reference = Math.abs(direction.y) < 0.9F ? new Vector3f(0.0F, 1.0F, 0.0F) : new Vector3f(1.0F, 0.0F, 0.0F);
        Vector3f side1 = new Vector3f(direction).cross(reference).normalize().mul(width);
        Vector3f side2 = new Vector3f(direction).cross(side1).normalize().mul(width);
        for (Vector3f side : new Vector3f[]{side1, side2}) {
            quad(light, m,
                    new Vector3f(from).add(side), new Vector3f(to).add(side),
                    new Vector3f(to).sub(side), new Vector3f(from).sub(side),
                    r, g, b, alpha);
        }
    }

    private static void quad(VertexConsumer light, Matrix4f m, Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                             float r, float g, float bl, float alpha) {
        light.addVertex(m, a.x, a.y, a.z).setColor(r, g, bl, alpha);
        light.addVertex(m, b.x, b.y, b.z).setColor(r, g, bl, alpha);
        light.addVertex(m, c.x, c.y, c.z).setColor(r, g, bl, alpha);
        light.addVertex(m, d.x, d.y, d.z).setColor(r, g, bl, alpha);
        light.addVertex(m, d.x, d.y, d.z).setColor(r, g, bl, alpha);
        light.addVertex(m, c.x, c.y, c.z).setColor(r, g, bl, alpha);
        light.addVertex(m, b.x, b.y, b.z).setColor(r, g, bl, alpha);
        light.addVertex(m, a.x, a.y, a.z).setColor(r, g, bl, alpha);
    }
}
