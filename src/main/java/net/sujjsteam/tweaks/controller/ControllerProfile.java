package net.sujjsteam.tweaks.controller;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ControllerProfile(
        ResourceKey<Level> dimension,
        int chargeTime,
        int color,
        int secondaryColor,
        int cooldown
) {
}
