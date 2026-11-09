package net.sujjsteam.tweaks.controller.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.sujjsteam.tweaks.controller.ControllerProfile;

public class DimensionalControllerStartEvent extends DimensionalControllerEvent implements ICancellableEvent {

    public DimensionalControllerStartEvent(ServerPlayer player, ControllerProfile profile) {
        super(player, profile);
    }
}
