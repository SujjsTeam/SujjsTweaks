package net.sujjsteam.tweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.sujjsteam.tweaks.block.entity.AltarRadioactiveBlockEntity;
import org.joml.Matrix4f;

public class AltarRadioactiveRenderer implements BlockEntityRenderer<AltarRadioactiveBlockEntity> {

    private static final float GREEN_R = 0.35F;
    private static final float GREEN_G = 1.0F;
    private static final float GREEN_B = 0.3F;

    public AltarRadioactiveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AltarRadioactiveBlockEntity altar, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!altar.isRitualActive() || altar.getLevel() == null) {
            return;
        }
        float t = altar.getRitualTicks() + partialTick;
        VertexConsumer light = bufferSource.getBuffer(RenderType.lightning());
        drawCircle(poseStack, light, t);
        drawBeam(poseStack, light, t);
        drawRays(poseStack, light, t);
        drawCrystal(altar, poseStack, bufferSource, t);
    }

    public boolean shouldRenderOffScreen(AltarRadioactiveBlockEntity altar) {
        return true;
    }

    public AABB getRenderBoundingBox(AltarRadioactiveBlockEntity altar) {
        BlockPos pos = altar.getBlockPos();
        return new AABB(pos).inflate(4.0).expandTowards(0.0, 48.0, 0.0);
    }

    private void drawCircle(PoseStack poseStack, VertexConsumer light, float t) {
        float strength = AltarRadioactiveBlockEntity.smooth(t, 0.0F, 35.0F)
                * (1.0F - AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.RELEASE + 5,
                AltarRadioactiveBlockEntity.RITUAL_DURATION));
        if (strength <= 0.01F) {
            return;
        }
        float flash = t >= AltarRadioactiveBlockEntity.RELEASE
                ? 1.0F + 1.5F * (1.0F - AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.RELEASE,
                AltarRadioactiveBlockEntity.RELEASE + 10))
                : 1.0F + 0.6F * AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.CLIMAX_START,
                AltarRadioactiveBlockEntity.RELEASE);
        float alpha = Math.min(1.0F, strength * flash);
        float r = AltarRadioactiveBlockEntity.CIRCLE_RADIUS;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.03F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(t * 1.2F + t * t * 0.012F));
        Matrix4f m = poseStack.last().pose();
        ring(light, m, r - 0.1F, r + 0.1F, 64, 0, 0.55F * alpha);
        ring(light, m, r - 0.85F, r - 0.75F, 48, 4, 0.4F * alpha);
        for (int i = 0; i < 6; i++) {
            double a1 = i * (Math.PI / 3.0);
            double a2 = (i + 2) * (Math.PI / 3.0);
            line(light, m,
                    (float) Math.cos(a1) * (r - 0.1F), (float) Math.sin(a1) * (r - 0.1F),
                    (float) Math.cos(a2) * (r - 0.1F), (float) Math.sin(a2) * (r - 0.1F),
                    0.05F, 0.45F * alpha);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.04F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-t * 2.0F));
        ring(light, poseStack.last().pose(), r + 0.45F, r + 0.6F, 40, 3, 0.35F * alpha);
        poseStack.popPose();
    }

    private void drawBeam(PoseStack poseStack, VertexConsumer light, float t) {
        float rise = AltarRadioactiveBlockEntity.smooth(t, 30.0F, 95.0F);
        float strength = rise * (1.0F - AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.RELEASE,
                AltarRadioactiveBlockEntity.RELEASE + 25));
        if (strength <= 0.01F) {
            return;
        }
        float climax = AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.CLIMAX_START,
                AltarRadioactiveBlockEntity.RELEASE);
        float burst = t >= AltarRadioactiveBlockEntity.RELEASE
                ? 2.0F * (1.0F - AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.RELEASE,
                AltarRadioactiveBlockEntity.RELEASE + 12))
                : 0.0F;
        float pulse = 1.0F + 0.15F * (float) Math.sin(t * 0.8F);
        float height = 1.0F + rise * 47.0F;
        float core = (0.05F + 0.1F * climax + 0.25F * burst) * pulse;
        float aura = (0.18F + 0.25F * climax + 0.6F * burst) * pulse;

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(t * 6.0F));
        Matrix4f m = poseStack.last().pose();
        prism(light, m, core, height, 1.0F, 1.0F, 1.0F, 0.6F * strength);
        prism(light, m, aura, height, GREEN_R, GREEN_G, GREEN_B, 0.25F * strength);
        poseStack.popPose();
    }

    private void drawRays(PoseStack poseStack, VertexConsumer light, float t) {
        float strength = AltarRadioactiveBlockEntity.smooth(t, 105.0F, AltarRadioactiveBlockEntity.RELEASE)
                * (1.0F - AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.RELEASE + 2,
                AltarRadioactiveBlockEntity.RELEASE + 25));
        if (strength <= 0.01F) {
            return;
        }
        float grow = AltarRadioactiveBlockEntity.smooth(t, 105.0F, AltarRadioactiveBlockEntity.RELEASE + 6);
        RandomSource rng = RandomSource.create(432L);
        int count = 6 + (int) (grow * 16.0F);

        poseStack.pushPose();
        poseStack.translate(0.5F, AltarRadioactiveBlockEntity.crystalHeight(Math.min(t, AltarRadioactiveBlockEntity.RELEASE)), 0.5F);
        for (int i = 0; i < count; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(rng.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(rng.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rng.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(rng.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(rng.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rng.nextFloat() * 360.0F + t * 1.5F));
            float rayLength = (rng.nextFloat() * 2.5F + 1.5F) * (0.4F + grow * 1.3F);
            float width = (rng.nextFloat() * 0.25F + 0.1F) * (0.5F + grow);
            Matrix4f m = poseStack.last().pose();
            float alpha = 0.9F * strength;
            float x1 = -0.866F * width;
            float z1 = -0.5F * width;
            float x2 = 0.866F * width;
            float z2 = -0.5F * width;
            float z3 = width;
            ray(light, m, alpha, x1, rayLength, z1, x2, rayLength, z2);
            ray(light, m, alpha, x2, rayLength, z2, 0.0F, rayLength, z3);
            ray(light, m, alpha, 0.0F, rayLength, z3, x1, rayLength, z1);
        }
        poseStack.popPose();
    }

    private void drawCrystal(AltarRadioactiveBlockEntity altar, PoseStack poseStack,
                                 MultiBufferSource bufferSource, float t) {
        ItemStack item = altar.getAltarItem();
        if (item.isEmpty()) {
            return;
        }
        float rise = AltarRadioactiveBlockEntity.smooth(t, 0.0F, AltarRadioactiveBlockEntity.AWAKENING_END);
        float climax = AltarRadioactiveBlockEntity.smooth(t, AltarRadioactiveBlockEntity.CLIMAX_START,
                AltarRadioactiveBlockEntity.RELEASE);
        float spin = t * 3.0F + t * t * 0.06F;
        float tremor = climax * 0.07F;
        float dx = (float) Math.sin(t * 2.7F) * tremor;
        float dz = (float) Math.cos(t * 3.1F) * tremor;
        float pulse = 1.0F + (0.05F + climax * 0.12F) * (float) Math.sin(t * (0.4F + climax * 1.2F));
        float scale = (0.9F + 0.5F * rise + 0.4F * climax) * pulse;

        poseStack.pushPose();
        poseStack.translate(0.5F + dx, AltarRadioactiveBlockEntity.crystalHeight(t), 0.5F + dz);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(t * 0.07F) * 12.0F));
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                item, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, altar.getLevel(), 0);
        poseStack.popPose();
    }

    private static void ring(VertexConsumer light, Matrix4f m, float innerRadius, float outerRadius,
                             int segments, int skipEvery, float alpha) {
        for (int i = 0; i < segments; i++) {
            if (skipEvery > 0 && i % skipEvery == 0) {
                continue;
            }
            double a0 = i * (Math.PI * 2.0 / segments);
            double a1 = (i + 1) * (Math.PI * 2.0 / segments);
            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);
            quad(light, m,
                    c0 * innerRadius, 0.0F, s0 * innerRadius,
                    c0 * outerRadius, 0.0F, s0 * outerRadius,
                    c1 * outerRadius, 0.0F, s1 * outerRadius,
                    c1 * innerRadius, 0.0F, s1 * innerRadius,
                    GREEN_R, GREEN_G, GREEN_B, alpha);
        }
    }

    private static void line(VertexConsumer light, Matrix4f m, float x1, float z1, float x2, float z2,
                              float width, float alpha) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        float rayLength = (float) Math.sqrt(dx * dx + dz * dz);
        if (rayLength < 0.0001F) {
            return;
        }
        float nx = -dz / rayLength * width;
        float nz = dx / rayLength * width;
        quad(light, m,
                x1 + nx, 0.0F, z1 + nz,
                x2 + nx, 0.0F, z2 + nz,
                x2 - nx, 0.0F, z2 - nz,
                x1 - nx, 0.0F, z1 - nz,
                GREEN_R, GREEN_G, GREEN_B, alpha);
    }

    private static void prism(VertexConsumer light, Matrix4f m, float half, float height,
                               float r, float g, float b, float alpha) {
        quad(light, m, -half, 0.0F, -half, half, 0.0F, -half, half, height, -half, -half, height, -half, r, g, b, alpha);
        quad(light, m, half, 0.0F, -half, half, 0.0F, half, half, height, half, half, height, -half, r, g, b, alpha);
        quad(light, m, half, 0.0F, half, -half, 0.0F, half, -half, height, half, half, height, half, r, g, b, alpha);
        quad(light, m, -half, 0.0F, half, -half, 0.0F, -half, -half, height, -half, -half, height, half, r, g, b, alpha);
    }

    private static void ray(VertexConsumer light, Matrix4f m, float alpha,
                             float x1, float y1, float z1, float x2, float y2, float z2) {
        light.addVertex(m, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
        light.addVertex(m, x1, y1, z1).setColor(GREEN_R, GREEN_G, GREEN_B, 0.0F);
        light.addVertex(m, x2, y2, z2).setColor(GREEN_R, GREEN_G, GREEN_B, 0.0F);
        light.addVertex(m, x2, y2, z2).setColor(GREEN_R, GREEN_G, GREEN_B, 0.0F);

        light.addVertex(m, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha);
        light.addVertex(m, x2, y2, z2).setColor(GREEN_R, GREEN_G, GREEN_B, 0.0F);
        light.addVertex(m, x1, y1, z1).setColor(GREEN_R, GREEN_G, GREEN_B, 0.0F);
        light.addVertex(m, x1, y1, z1).setColor(GREEN_R, GREEN_G, GREEN_B, 0.0F);
    }

    private static void quad(VertexConsumer light, Matrix4f m,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float r, float g, float b, float alpha) {
        light.addVertex(m, x1, y1, z1).setColor(r, g, b, alpha);
        light.addVertex(m, x2, y2, z2).setColor(r, g, b, alpha);
        light.addVertex(m, x3, y3, z3).setColor(r, g, b, alpha);
        light.addVertex(m, x4, y4, z4).setColor(r, g, b, alpha);

        light.addVertex(m, x4, y4, z4).setColor(r, g, b, alpha);
        light.addVertex(m, x3, y3, z3).setColor(r, g, b, alpha);
        light.addVertex(m, x2, y2, z2).setColor(r, g, b, alpha);
        light.addVertex(m, x1, y1, z1).setColor(r, g, b, alpha);
    }
}
