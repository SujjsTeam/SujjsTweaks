package net.sujjsteam.tweaks.registry;

import com.mojang.serialization.Codec;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.sujjsteam.tweaks.Tweaks;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Tweaks.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> RADIATION_STABILIZED = ATTACHMENT_TYPES.register(
            "radiation_stabilized",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Vec3>> DIMENSION_RETURN_POS = ATTACHMENT_TYPES.register(
            "dimension_return_pos",
            () -> AttachmentType.builder(() -> Vec3.ZERO).serialize(Vec3.CODEC).copyOnDeath().build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Vec3>> DIMENSION_RETURN_ROT = ATTACHMENT_TYPES.register(
            "dimension_return_rot",
            () -> AttachmentType.builder(() -> Vec3.ZERO).serialize(Vec3.CODEC).copyOnDeath().build()
    );
}
