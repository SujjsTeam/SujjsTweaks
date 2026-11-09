package net.sujjsteam.tweaks.ritual;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class RitualWeather {

    private static final float FADE_PER_TICK = 0.02F;
    private static final Map<ResourceKey<Level>, Long> LAST_ACTIVE = new HashMap<>();

    private RitualWeather() {
    }

    public static boolean ownsWeather(ServerLevel level) {
        return Level.OVERWORLD.equals(level.dimension());
    }

    public static void force(ServerLevel level, float strength) {
        level.setRainLevel(strength);
        level.setThunderLevel(strength);
        LAST_ACTIVE.put(level.dimension(), level.getGameTime());
    }

    public static void tick(ServerLevel level) {
        Long last = LAST_ACTIVE.get(level.dimension());
        if (last == null || level.getGameTime() - last <= 1) {
            return;
        }
        float rain = level.getRainLevel(1.0F);
        float thunder = level.getThunderLevel(1.0F);
        if (rain > 0.0F || thunder > 0.0F) {
            level.setRainLevel(Math.max(0.0F, rain - FADE_PER_TICK));
            level.setThunderLevel(Math.max(0.0F, thunder - FADE_PER_TICK));
        } else {
            LAST_ACTIVE.remove(level.dimension());
        }
    }
}
