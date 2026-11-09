package net.sujjsteam.tweaks.controller.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.sujjsteam.tweaks.controller.ControllerProfile;

public abstract class DimensionalControllerEvent extends Event {

    private final ServerPlayer player;
    private final ControllerProfile profile;

    protected DimensionalControllerEvent(ServerPlayer player, ControllerProfile profile) {
        this.player = player;
        this.profile = profile;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getDimension() {
        return profile.dimension().location();
    }

    public int getChargeTime() {
        return profile.chargeTime();
    }
}
