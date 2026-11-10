package net.sujjsteam.tweaks.compat.scguns;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import top.ribs.scguns.item.animated.AnimatedGunItem;

public class RadioactiveMinigunItem extends AnimatedGunItem {

    public RadioactiveMinigunItem(Properties properties, String modelName, SoundEvent magOut, SoundEvent magIn,
                                  SoundEvent reloadEnd, SoundEvent jam, SoundEvent jamEnd) {
        super(properties, modelName, magOut, magIn, reloadEnd, jam, jamEnd);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }
}
