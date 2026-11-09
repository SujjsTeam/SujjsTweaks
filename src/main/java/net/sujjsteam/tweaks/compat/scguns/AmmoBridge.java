package net.sujjsteam.tweaks.compat.scguns;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.registry.ModItems;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Tweaks.MOD_ID)
public final class AmmoBridge {

    public static final boolean DEBUG = false;
    public static final String SPECIAL_LOADED = "TweaksSpecialLoaded";
    public static final String SPECIAL_KIND = "TweaksSpecialKind";

    private enum Source { UNIVERSAL, INFINITE, OFFICIAL }

    private record Pending(ItemStack stack, int initial, Source source) {
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, List<Pending>> PENDING = new HashMap<>();

    private AmmoBridge() {
    }

    public static void debug(String message, Object... args) {
        if (DEBUG) {
            LOGGER.info("[TweaksAmmo] " + message, args);
        }
    }

    public static boolean hasInfinite(Player player) {
        return player != null && player.getInventory().contains(stack -> stack.is(ModItems.INFINITE_AMMO.get()));
    }

    private static int count(Player player, Item item) {
        if (player == null || item == null) {
            return 0;
        }
        Inventory inventory = player.getInventory();
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static int universalCount(Player player) {
        return count(player, ModItems.UNIVERSAL_AMMO.get());
    }

    private static void track(Player player, ItemStack stack, Source source) {
        if (player != null && !player.level().isClientSide && stack != null && !stack.isEmpty()) {
            PENDING.computeIfAbsent(player.getUUID(), id -> new ArrayList<>()).add(new Pending(stack, stack.getCount(), source));
        }
    }

    public static ItemStack infiniteStack(Player player, Item ammo) {
        if (ammo == null || ammo == Items.AIR) {
            return null;
        }
        ItemStack virtual = new ItemStack(ammo, Integer.MAX_VALUE);
        track(player, virtual, Source.INFINITE);
        return virtual;
    }

    public static ItemStack universalStack(Player player, Item ammo) {
        if (player == null || ammo == null || ammo == Items.AIR || ammo == ModItems.UNIVERSAL_AMMO.get()) {
            return null;
        }
        int universal = universalCount(player);
        if (universal <= 0) {
            return null;
        }
        ItemStack virtual = new ItemStack(ammo, universal);
        track(player, virtual, Source.UNIVERSAL);
        return virtual;
    }

    public static void trackOfficial(Player player, ItemStack[] stacks) {
        if (stacks == null) {
            return;
        }
        for (ItemStack stack : stacks) {
            track(player, stack, Source.OFFICIAL);
        }
    }

    public static ItemStack displayIcon(Player player, ItemStack original) {
        if (player == null || original == null || original.isEmpty()) {
            return original;
        }
        if (hasInfinite(player)) {
            return new ItemStack(ModItems.INFINITE_AMMO.get());
        }
        ItemStack gun = player.getMainHandItem();
        int loaded = gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("AmmoCount");
        if (loaded > 0) {
            if (isSpecialLoaded(gun)) {
                return new ItemStack(isInfiniteKind(gun) ? ModItems.INFINITE_AMMO.get() : ModItems.UNIVERSAL_AMMO.get());
            }
            return original;
        }
        if (universalCount(player) > 0 && count(player, original.getItem()) == 0) {
            return new ItemStack(ModItems.UNIVERSAL_AMMO.get());
        }
        return original;
    }

    public static boolean isInfiniteKind(ItemStack gun) {
        return gun != null && !gun.isEmpty()
                && "infinite".equals(gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(SPECIAL_KIND));
    }

    public static boolean isSpecialLoaded(ItemStack gun) {
        return gun != null && !gun.isEmpty()
                && gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(SPECIAL_LOADED);
    }

    private static void setSpecialLoaded(ItemStack gun, boolean special, boolean infiniteKind) {
        if (gun.isEmpty() || gun.is(ModItems.UNIVERSAL_AMMO.get()) || gun.is(ModItems.INFINITE_AMMO.get())) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, gun, tag -> {
            if (special) {
                tag.putBoolean(SPECIAL_LOADED, true);
                tag.putString(SPECIAL_KIND, infiniteKind ? "infinite" : "universal");
            } else {
                tag.remove(SPECIAL_LOADED);
                tag.remove(SPECIAL_KIND);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<Pending> list = PENDING.remove(player.getUUID());
        if (list == null) {
            return;
        }
        int universalUsed = 0;
        int infiniteUsed = 0;
        int officialUsed = 0;
        for (Pending pending : list) {
            int used = Math.max(0, pending.initial() - pending.stack().getCount());
            switch (pending.source()) {
                case UNIVERSAL -> universalUsed += used;
                case INFINITE -> infiniteUsed += used;
                case OFFICIAL -> officialUsed += used;
            }
        }
        universalUsed = Math.min(universalUsed, universalCount(player));
        if (universalUsed > 0) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(ModItems.UNIVERSAL_AMMO.get()),
                    universalUsed, player.inventoryMenu.getCraftSlots());
        }
        int special = universalUsed + infiniteUsed;
        if (special + officialUsed > 0) {
            boolean loadedSpecial = special >= officialUsed;
            setSpecialLoaded(player.getMainHandItem(), loadedSpecial, infiniteUsed > universalUsed);
            debug("reload: Universal {}, Infinite {}, official {} -> gun marked special {}", universalUsed, infiniteUsed, officialUsed, loadedSpecial);
        }
    }

    public static boolean hideAnimatedBullet(Player player, ItemStack gun) {
        if (gun == null || gun.isEmpty()) {
            return false;
        }
        if (isSpecialLoaded(gun)) {
            return true;
        }
        return player != null && hasInfinite(player) && player.getMainHandItem().is(gun.getItem());
    }

    public static boolean skipCasing(Player player) {
        if (player == null) {
            return false;
        }
        boolean skip = hasInfinite(player) || isSpecialLoaded(player.getMainHandItem());
        debug("casing item ({}): skip {}", player.level().isClientSide ? "client" : "server", skip);
        return skip;
    }
}
