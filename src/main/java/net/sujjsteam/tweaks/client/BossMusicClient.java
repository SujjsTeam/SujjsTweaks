package net.sujjsteam.tweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.entity.Nucleon;
import net.sujjsteam.tweaks.registry.ModSounds;

@EventBusSubscriber(modid = Tweaks.MOD_ID, value = Dist.CLIENT)
public class BossMusicClient {

    private static final double RANGE = 25.0;

    private static Music battleMusic;
    private static boolean wasNear;

    private static Music battleMusic() {
        if (battleMusic == null) {
            battleMusic = new Music(ModSounds.NUCLEON_BATTLE_MUSIC, 0, 0, true);
        }
        return battleMusic;
    }

    private static boolean bossNear() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return false;
        }
        for (Nucleon boss : minecraft.level.getEntitiesOfClass(Nucleon.class,
                minecraft.player.getBoundingBox().inflate(RANGE), Nucleon::isAlive)) {
            if (boss.distanceToSqr(minecraft.player) <= RANGE * RANGE) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onSelectMusic(SelectMusicEvent event) {
        if (bossNear()) {
            event.setMusic(battleMusic());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean near = bossNear();
        if (wasNear && !near) {
            Minecraft.getInstance().getMusicManager().stopPlaying(battleMusic());
        }
        wasNear = near;
    }
}
