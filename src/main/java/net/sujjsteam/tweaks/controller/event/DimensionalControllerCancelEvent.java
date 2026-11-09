package net.sujjsteam.tweaks.controller.event;

import net.minecraft.server.level.ServerPlayer;
import net.sujjsteam.tweaks.controller.ControllerProfile;

public class DimensionalControllerCancelEvent extends DimensionalControllerEvent {

    public DimensionalControllerCancelEvent(ServerPlayer player, ControllerProfile profile) {
        super(player, profile);
    }
}
