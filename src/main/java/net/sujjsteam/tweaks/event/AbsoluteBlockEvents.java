package net.sujjsteam.tweaks.event;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.block.TheAbsoluteBlock;
import net.sujjsteam.tweaks.registry.ModBlocks;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public class AbsoluteBlockEvents {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getState().is(ModBlocks.THE_ABSOLUTE_BLOCK.get())) {
            return;
        }
        Player player = event.getEntity();
        boolean correctTool = event.getPosition()
                .map(pos -> player.hasCorrectToolForDrops(event.getState(), player.level(), pos))
                .orElseGet(() -> player.getMainHandItem().isCorrectToolForDrops(event.getState()));
        if (!correctTool) {
            event.setNewSpeed(0.0F);
            return;
        }
        float efficiency = (float) player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        event.setNewSpeed(TheAbsoluteBlock.BASE_SPEED + efficiency);
    }
}
