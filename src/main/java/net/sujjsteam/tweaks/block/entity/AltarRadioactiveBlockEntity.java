package net.sujjsteam.tweaks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.sujjsteam.tweaks.block.AltarRadioactiveBlock;
import net.sujjsteam.tweaks.entity.Nucleon;
import net.sujjsteam.tweaks.registry.ModBlockEntities;
import net.sujjsteam.tweaks.registry.ModEntities;
import net.sujjsteam.tweaks.registry.ModItems;
import net.sujjsteam.tweaks.registry.ModSounds;
import net.sujjsteam.tweaks.ritual.RitualSky;
import net.sujjsteam.tweaks.ritual.RitualWeather;
import net.sujjsteam.tweaks.world.BossRegistry;
import org.joml.Vector3f;

public class AltarRadioactiveBlockEntity extends BlockEntity {

    public static final int AWAKENING_END = 40;
    public static final int CLIMAX_START = 115;
    public static final int RELEASE = 150;
    public static final int RITUAL_DURATION = 180;
    public static final float CIRCLE_RADIUS = 2.5F;

    private static final DustParticleOptions GREEN_DUST =
            new DustParticleOptions(new Vector3f(0.35F, 1.0F, 0.25F), 1.0F);
    private static final DustParticleOptions BIG_GREEN_DUST =
            new DustParticleOptions(new Vector3f(0.45F, 1.0F, 0.3F), 2.2F);
    private static final DustParticleOptions GOLD_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.85F, 0.3F), 1.2F);

    private ItemStack altarItem = ItemStack.EMPTY;
    private int ritualTicks = -1;
    private boolean weatherSaved;
    private boolean wasRaining;
    private boolean wasThundering;

    public AltarRadioactiveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR_RADIOACTIVE.get(), pos, state);
    }

    public boolean isRitualActive() {
        return ritualTicks >= 0;
    }

    public ItemStack getAltarItem() {
        return altarItem;
    }

    public int getRitualTicks() {
        return ritualTicks;
    }

    public static float smooth(float time, float start, float end) {
        float x = Math.max(0.0F, Math.min(1.0F, (time - start) / (end - start)));
        return x * x * (3.0F - 2.0F * x);
    }

    public static float crystalHeight(float time) {
        return 1.15F + smooth(time, 0.0F, AWAKENING_END) * 1.35F + (float) Math.sin(time * 0.12F) * 0.06F;
    }

    public void startRitual(ItemStack item) {
        if (level == null) {
            return;
        }
        altarItem = item;
        ritualTicks = 0;
        if (level instanceof ServerLevel serverLevel) {
            summonStorm(serverLevel);
        }
        setLit(true);
        syncToClients();
        playRitualSound(ModSounds.RITUAL_START.get(), 3.0F, 1.0F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AltarRadioactiveBlockEntity altar) {
        if (!altar.isRitualActive()) {
            return;
        }
        altar.ritualTicks++;
        if (level instanceof ServerLevel serverLevel) {
            altar.serverRitualTick(serverLevel);
        } else {
            altar.clientParticles(level);
        }
    }

    private void serverRitualTick(ServerLevel serverLevel) {
        if (!RitualWeather.ownsWeather(serverLevel)) {
            float storm = smooth(ritualTicks, 0.0F, 60.0F);
            RitualWeather.force(serverLevel, storm);
        }
        float progress = smooth(ritualTicks, AWAKENING_END, RELEASE);

        if (ritualTicks >= AWAKENING_END && ritualTicks < RELEASE) {
            float climax = smooth(ritualTicks, CLIMAX_START, RELEASE);
            if (serverLevel.random.nextFloat() < 0.035F + climax * 0.09F) {
                strikeAround(serverLevel);
            }
        }
        if (ritualTicks < RELEASE && ritualTicks % 20 == 0) {
            playRitualSound(ModSounds.RITUAL_PULSE.get(), 2.5F, 0.7F + progress * 0.9F);
        }
        if (ritualTicks == CLIMAX_START - 5) {
            playRitualSound(ModSounds.RITUAL_CLIMAX.get(), 3.0F, 1.0F);
        }
        if (ritualTicks == RELEASE) {
            release(serverLevel);
        }
        if (ritualTicks >= RITUAL_DURATION) {
            playRitualSound(SoundEvents.BEACON_DEACTIVATE, 1.5F, 0.6F);
            restoreWeather(serverLevel);
            ritualTicks = -1;
            setLit(false);
            syncToClients();
        }
    }

    private void release(ServerLevel serverLevel) {
        double cx = worldPosition.getX() + 0.5;
        double cy = worldPosition.getY() + crystalHeight(RELEASE);
        double cz = worldPosition.getZ() + 0.5;

        serverLevel.sendParticles(ParticleTypes.FLASH, cx, cy, cz, 3, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, cx, cy, cz, 1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles(BIG_GREEN_DUST, cx, cy, cz, 150, 1.4, 1.4, 1.4, 0.0);

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(worldPosition.above()));
            bolt.setVisualOnly(true);
            serverLevel.addFreshEntity(bolt);
        }
        playRitualSound(ModSounds.RITUAL_RELEASE.get(), 5.0F, 1.0F);

        altarItem = ItemStack.EMPTY;
        syncToClients();
        onRitualFinished(serverLevel);
    }

    protected void onRitualFinished(ServerLevel serverLevel) {
        BossRegistry registry = BossRegistry.get(serverLevel);
        if (registry.distanceToNearest(serverLevel, worldPosition, Nucleon.SINGLE_BOSS_RADIUS).isPresent()) {
            Block.popResource(serverLevel, worldPosition.above(), new ItemStack(ModItems.CRYSTAL_OF_FIRE.get()));
            playRitualSound(SoundEvents.BEACON_DEACTIVATE, 1.5F, 0.5F);
            return;
        }
        Nucleon boss = ModEntities.NUCLEON.get().create(serverLevel);
        if (boss != null) {
            boss.moveTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                    serverLevel.random.nextFloat() * 360.0F, 0.0F);
            boss.setPersistenceRequired();
            serverLevel.addFreshEntity(boss);
            registry.update(boss.getUUID(), boss.blockPosition());
        }
    }

    private void summonStorm(ServerLevel serverLevel) {
        if (!weatherSaved) {
            wasRaining = serverLevel.isRaining();
            wasThundering = serverLevel.isThundering();
            weatherSaved = true;
        }
        serverLevel.setWeatherParameters(0, RITUAL_DURATION + 220, true, true);
    }

    private void restoreWeather(ServerLevel serverLevel) {
        if (!weatherSaved) {
            return;
        }
        if (!wasRaining) {
            serverLevel.setWeatherParameters(6000 + serverLevel.random.nextInt(6000), 0, false, false);
        } else if (!wasThundering) {
            serverLevel.setWeatherParameters(0, 3000 + serverLevel.random.nextInt(3000), true, false);
        }
        weatherSaved = false;
    }

    private void strikeAround(ServerLevel serverLevel) {
        double angle = serverLevel.random.nextDouble() * Math.PI * 2.0;
        double dist = 8.0 + serverLevel.random.nextDouble() * 16.0;
        BlockPos target = BlockPos.containing(
                worldPosition.getX() + 0.5 + Math.cos(angle) * dist,
                worldPosition.getY(),
                worldPosition.getZ() + 0.5 + Math.sin(angle) * dist);
        BlockPos ground = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, target);
        if (ground.getY() < worldPosition.getY() - 8) {
            ground = new BlockPos(target.getX(), worldPosition.getY(), target.getZ());
        }
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(ground));
            bolt.setVisualOnly(true);
            serverLevel.addFreshEntity(bolt);
        }
    }

    public static float skyStrength(float time) {
        return smooth(time, 0.0F, 70.0F) * (1.0F - smooth(time, RELEASE + 5, RITUAL_DURATION));
    }

    public static float skyFlash(float time) {
        if (time < RELEASE || time > RELEASE + 8) {
            return 0.0F;
        }
        return 1.0F - (time - RELEASE) / 8.0F;
    }

    private void clientParticles(Level level) {
        RandomSource r = level.random;
        float t = ritualTicks;
        double cx = worldPosition.getX() + 0.5;
        double base = worldPosition.getY();
        double cz = worldPosition.getZ() + 0.5;
        double crystalY = base + crystalHeight(t);

        RitualSky.report(cx, base, cz, skyStrength(t), skyFlash(t));

        if (t < RELEASE) {
            float ring = smooth(t, 0.0F, 35.0F);
            for (int i = 0; i < 3; i++) {
                if (r.nextFloat() < ring) {
                    double a = r.nextDouble() * Math.PI * 2.0;
                    level.addParticle(GREEN_DUST,
                            cx + Math.cos(a) * CIRCLE_RADIUS, base + 0.1 + r.nextDouble() * 0.3, cz + Math.sin(a) * CIRCLE_RADIUS,
                            0.0, 0.0, 0.0);
                }
            }

            if (t >= 15) {
                int runes = 2 + (int) (smooth(t, 15.0F, 140.0F) * 7.0F);
                for (int i = 0; i < runes; i++) {
                    double a = r.nextDouble() * Math.PI * 2.0;
                    double radius = CIRCLE_RADIUS + r.nextDouble() * 0.8;
                    double sx = cx + Math.cos(a) * radius;
                    double sy = base + r.nextDouble() * 0.6;
                    double sz = cz + Math.sin(a) * radius;
                    level.addParticle(ParticleTypes.ENCHANT, cx, crystalY, cz, sx - cx, sy - crystalY, sz - cz);
                }
            }

            if (t >= AWAKENING_END) {
                float charge = smooth(t, AWAKENING_END, RELEASE);
                double spiralRadius = 1.9 - charge * 1.35;
                double height = crystalY - base - 0.2;
                for (int arm = 0; arm < 3; arm++) {
                    for (int k = 0; k < 3; k++) {
                        double h = (t * (0.03 + charge * 0.03) + arm / 3.0 + k / 3.0) % 1.0;
                        double a = t * (0.12 + charge * 0.35) + arm * (Math.PI * 2.0 / 3.0) + h * Math.PI * 2.0;
                        double rk = spiralRadius * (1.0 - h * 0.55);
                        level.addParticle(k == 0 ? GOLD_DUST : GREEN_DUST,
                                cx + Math.cos(a) * rk, base + 0.2 + h * height, cz + Math.sin(a) * rk,
                                0.0, 0.0, 0.0);
                    }
                }
                if (r.nextFloat() < 0.4F + charge * 0.6F) {
                    double a = r.nextDouble() * Math.PI * 2.0;
                    double sx = cx + Math.cos(a) * 1.3;
                    double sz = cz + Math.sin(a) * 1.3;
                    double sy = crystalY + (r.nextDouble() - 0.5) * 0.8;
                    level.addParticle(ParticleTypes.FLAME, sx, sy, sz, (cx - sx) * 0.07, (crystalY - sy) * 0.07, (cz - sz) * 0.07);
                }
            }

            if (t >= CLIMAX_START) {
                float climax = smooth(t, CLIMAX_START, RELEASE);
                int sparks = 1 + (int) (climax * 5.0F);
                for (int i = 0; i < sparks; i++) {
                    level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                            cx + (r.nextDouble() - 0.5) * 0.8, crystalY + (r.nextDouble() - 0.5) * 0.8, cz + (r.nextDouble() - 0.5) * 0.8,
                            (r.nextDouble() - 0.5) * 0.4, (r.nextDouble() - 0.5) * 0.4, (r.nextDouble() - 0.5) * 0.4);
                }
                if (r.nextFloat() < climax) {
                    double a = r.nextDouble() * Math.PI * 2.0;
                    level.addParticle(ParticleTypes.END_ROD,
                            cx + Math.cos(a) * 3.0, crystalY + (r.nextDouble() - 0.5) * 2.0, cz + Math.sin(a) * 3.0,
                            -Math.cos(a) * 0.14, 0.0, -Math.sin(a) * 0.14);
                }
                if (ritualTicks % 2 == 0) {
                    for (int j = 0; j < 16; j++) {
                        double a = j * (Math.PI / 8.0) + t * 0.08;
                        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                                cx + Math.cos(a) * CIRCLE_RADIUS, base + 0.05, cz + Math.sin(a) * CIRCLE_RADIUS,
                                0.0, 0.03 + climax * 0.05, 0.0);
                    }
                }
            }
        } else {
            double dt = t - RELEASE;
            double radius = 0.8 + dt * 0.45;
            double falloff = Math.max(0.0, 1.0 - dt / 30.0);
            int points = 56;
            for (int j = 0; j < points; j++) {
                double a = j * (Math.PI * 2.0 / points) + r.nextDouble() * 0.05;
                double x = cx + Math.cos(a) * radius;
                double z = cz + Math.sin(a) * radius;
                level.addParticle(BIG_GREEN_DUST, x, base + 0.15, z, 0.0, 0.0, 0.0);
                if (j % 4 == 0) {
                    level.addParticle(ParticleTypes.CLOUD, x, base + 0.2, z,
                            Math.cos(a) * 0.18 * falloff, 0.02, Math.sin(a) * 0.18 * falloff);
                }
            }
            if (dt < 14) {
                double releaseY = base + crystalHeight(RELEASE);
                for (int i = 0; i < 8; i++) {
                    level.addParticle(ParticleTypes.END_ROD, cx, releaseY, cz,
                            (r.nextDouble() - 0.5) * 0.5, r.nextDouble() * 0.6, (r.nextDouble() - 0.5) * 0.5);
                }
            }
        }
    }

    private void playRitualSound(SoundEvent sound, float volume, float pitch) {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitch);
        }
    }

    private void setLit(boolean lit) {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(AltarRadioactiveBlock.LIT) && state.getValue(AltarRadioactiveBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(AltarRadioactiveBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    private void syncToClients() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("RitualTicks", ritualTicks);
        tag.putBoolean("WeatherSaved", weatherSaved);
        tag.putBoolean("WasRaining", wasRaining);
        tag.putBoolean("WasThundering", wasThundering);
        if (!altarItem.isEmpty()) {
            tag.put("AltarItem", altarItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ritualTicks = tag.contains("RitualTicks") ? tag.getInt("RitualTicks") : -1;
        weatherSaved = tag.getBoolean("WeatherSaved");
        wasRaining = tag.getBoolean("WasRaining");
        wasThundering = tag.getBoolean("WasThundering");
        altarItem = tag.contains("AltarItem")
                ? ItemStack.parseOptional(registries, tag.getCompound("AltarItem"))
                : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
