package net.sujjsteam.tweaks.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.controller.ControllerProfile;

public final class RadioactiveDimension {

    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(Tweaks.MOD_ID, "radioactive"));

    public static final ControllerProfile CONTROLLER_PROFILE = new ControllerProfile(DIMENSION, 100, 0x39FF14, 0xD8FF60, 100);
    public static final ControllerProfile OVERWORLD_JUMP_PROFILE = new ControllerProfile(Level.OVERWORLD, 100, 0x39FF14, 0xD8FF60, 100);

    public static final float JUMP_CHANCE = 0.05F;
    public static final int MAX_JUMP_ATTEMPTS = 12;

    private RadioactiveDimension() {
    }

    public static ControllerProfile controllerProfile(ResourceKey<Level> dimension) {
        if (DIMENSION.equals(dimension)) {
            return CONTROLLER_PROFILE;
        }
        if (Level.OVERWORLD.equals(dimension)) {
            return OVERWORLD_JUMP_PROFILE;
        }
        return null;
    }
}
