package net.sujjsteam.tweaks.controller.event;

import net.minecraft.server.level.ServerPlayer;
import net.sujjsteam.tweaks.controller.ControllerProfile;

public class DimensionalControllerCompleteEvent extends DimensionalControllerEvent {

    public DimensionalControllerCompleteEvent(ServerPlayer player, ControllerProfile profile) {
        super(player, profile);
    }
}
