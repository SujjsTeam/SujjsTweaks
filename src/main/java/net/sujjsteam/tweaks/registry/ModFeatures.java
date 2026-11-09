package net.sujjsteam.tweaks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.sujjsteam.tweaks.Tweaks;
import net.sujjsteam.tweaks.world.feature.RadioactiveSurfaceFeature;

import java.util.Optional;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Tweaks.MOD_ID);

    public static final DeferredHolder<Feature<?>, RadioactiveSurfaceFeature> RADIOACTIVE_SURFACE =
            FEATURES.register("radioactive_surface", RadioactiveSurfaceFeature::new);

    public static final ResourceKey<ConfiguredFeature<?, ?>> RADIOACTIVE_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Tweaks.MOD_ID, "radioactive_tree"));

    public static final TreeGrower RADIOACTIVE_TREE_GROWER =
            new TreeGrower("sujjstweaks_radioactive", Optional.empty(), Optional.of(RADIOACTIVE_TREE), Optional.empty());
}
