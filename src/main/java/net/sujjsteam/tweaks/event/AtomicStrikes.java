package net.sujjsteam.tweaks.event;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.sujjsteam.tweaks.Tweaks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public class AtomicStrikes {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int TNT_COUNT = 1500;
    public static final int TNT_PER_TICK = 100;
    public static final double RAIN_RADIUS = 10.0;
    public static final double RAIN_HEIGHT_MIN = 25.0;
    public static final double RAIN_HEIGHT_EXTRA = 20.0;
    public static final int FUSE_MIN = 60;
    public static final int FUSE_EXTRA = 50;

    private static final List<Strike> STRIKES = new ArrayList<>();

    private static final class Strike {
        final ResourceKey<Level> dimension;
        final Vec3 target;
        final UUID owner;
        int remaining = TNT_COUNT;
        int spawned = 0;

        Strike(ResourceKey<Level> dimension, Vec3 target, UUID owner) {
            this.dimension = dimension;
            this.target = target;
            this.owner = owner;
        }
    }

    public static void start(ServerLevel level, Vec3 target, LivingEntity owner) {
        STRIKES.add(new Strike(level.dimension(), target, owner == null ? null : owner.getUUID()));
        LOGGER.info("[Atomic Bomb] Ataque iniciado em {} ({}, {}, {}) com {} TNTs",
                level.dimension().location(), (int) target.x, (int) target.y, (int) target.z, TNT_COUNT);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (STRIKES.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        Iterator<Strike> iterator = STRIKES.iterator();
        while (iterator.hasNext()) {
            Strike strike = iterator.next();
            ServerLevel level = server.getLevel(strike.dimension);
            if (level == null) {
                LOGGER.warn("[Atomic Bomb] Dimensão {} não encontrada, ataque cancelado", strike.dimension.location());
                iterator.remove();
                continue;
            }
            RandomSource random = level.getRandom();
            LivingEntity owner = strike.owner == null ? null : level.getPlayerByUUID(strike.owner);
            int batch = Math.min(TNT_PER_TICK, strike.remaining);
            for (int i = 0; i < batch; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double distance = Math.sqrt(random.nextDouble()) * RAIN_RADIUS;
                double x = strike.target.x + Math.cos(angle) * distance;
                double z = strike.target.z + Math.sin(angle) * distance;
                double y = strike.target.y + RAIN_HEIGHT_MIN + random.nextDouble() * RAIN_HEIGHT_EXTRA;
                PrimedTnt tnt = new PrimedTnt(level, x, y, z, owner);
                tnt.setFuse(FUSE_MIN + random.nextInt(FUSE_EXTRA));
                tnt.setDeltaMovement((random.nextDouble() - 0.5) * 0.1, -0.2, (random.nextDouble() - 0.5) * 0.1);
                if (level.addFreshEntity(tnt)) {
                    strike.spawned++;
                }
            }
            strike.remaining -= batch;
            if (strike.remaining <= 0) {
                LOGGER.info("[Atomic Bomb] Ataque terminado: {} de {} TNTs entraram no mundo", strike.spawned, TNT_COUNT);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        STRIKES.clear();
    }
}
