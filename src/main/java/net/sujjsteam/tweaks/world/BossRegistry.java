package net.sujjsteam.tweaks.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.sujjsteam.tweaks.entity.Nucleon;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BossRegistry extends SavedData {

    private static final String NAME = "sujjstweaks_bosses";

    private final Map<UUID, BlockPos> bosses = new HashMap<>();

    public static BossRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BossRegistry::new, BossRegistry::load, null), NAME);
    }

    private static BossRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        BossRegistry registry = new BossRegistry();
        ListTag list = tag.getList("Bosses", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag item = list.getCompound(i);
            registry.bosses.put(item.getUUID("Id"), BlockPos.of(item.getLong("Pos")));
        }
        return registry;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        bosses.forEach((id, pos) -> {
            CompoundTag item = new CompoundTag();
            item.putUUID("Id", id);
            item.putLong("Pos", pos.asLong());
            list.add(item);
        });
        tag.put("Bosses", list);
        return tag;
    }

    public void update(UUID id, BlockPos pos) {
        BlockPos previous = bosses.put(id, pos.immutable());
        if (!pos.equals(previous)) {
            setDirty();
        }
    }

    public void unregister(UUID id) {
        if (bosses.remove(id) != null) {
            setDirty();
        }
    }

    public Optional<Double> distanceToNearest(ServerLevel level, BlockPos origin, double radius) {
        removeStale(level);
        double closest = Double.MAX_VALUE;
        for (BlockPos pos : bosses.values()) {
            double dx = pos.getX() - origin.getX();
            double dz = pos.getZ() - origin.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist <= radius && dist < closest) {
                closest = dist;
            }
        }
        return closest == Double.MAX_VALUE ? Optional.empty() : Optional.of(closest);
    }

    private void removeStale(ServerLevel level) {
        boolean changed = bosses.entrySet().removeIf(entry ->
                level.areEntitiesLoaded(ChunkPos.asLong(entry.getValue()))
                        && !(level.getEntity(entry.getKey()) instanceof Nucleon boss && boss.isAlive()));
        if (changed) {
            setDirty();
        }
    }
}
