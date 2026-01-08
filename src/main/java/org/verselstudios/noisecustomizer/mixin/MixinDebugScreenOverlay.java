package org.verselstudios.noisecustomizer.mixin;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.network.Connection;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private ChunkPos lastPos;

    @Final
    @Shadow
    private static Map<Heightmap.Types, String> HEIGHTMAP_NAMES;

//    @Inject(method = "getGameInformation", at = @At(target = "Ljava/util/List;add", ordinal = 4, value = "INVOKE"))
//    public void getMoreGameInformation(CallbackInfoReturnable<List<String>> list) {
//
//    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected List<String> getGameInformation() {
        IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
        ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
        Connection connection = clientpacketlistener.getConnection();
        float f = connection.getAverageSentPackets();
        float f1 = connection.getAverageReceivedPackets();
        TickRateManager tickratemanager = this.getLevel().tickRateManager();
        String s1;
        if (tickratemanager.isSteppingForward()) {
            s1 = " (frozen - stepping)";
        } else if (tickratemanager.isFrozen()) {
            s1 = " (frozen)";
        } else {
            s1 = "";
        }

        String s;
        if (integratedserver != null) {
            ServerTickRateManager servertickratemanager = integratedserver.tickRateManager();
            boolean flag = servertickratemanager.isSprinting();
            if (flag) {
                s1 = " (sprinting)";
            }

            String s2 = flag ? "-" : String.format(Locale.ROOT, "%.1f", tickratemanager.millisecondsPerTick());
            s = String.format(Locale.ROOT, "Integrated server @ %.1f/%s ms%s, %.0f tx, %.0f rx", integratedserver.getCurrentSmoothedTickTime(), s2, s1, f, f1);
        } else {
            s = String.format(Locale.ROOT, "\"%s\" server%s, %.0f tx, %.0f rx", clientpacketlistener.serverBrand(), s1, f, f1);
        }

        BlockPos blockpos = this.minecraft.getCameraEntity().blockPosition();
        String var10003;
        String[] var32;
        if (this.minecraft.showOnlyReducedInfo()) {
            var32 = new String[9];
            var10003 = SharedConstants.getCurrentVersion().getName();
            var32[0] = "Minecraft " + var10003 + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")";
            var32[1] = this.minecraft.fpsString;
            var32[2] = s;
            var32[3] = this.minecraft.levelRenderer.getSectionStatistics();
            var32[4] = this.minecraft.levelRenderer.getEntityStatistics();
            var10003 = this.minecraft.particleEngine.countParticles();
            var32[5] = "P: " + var10003 + ". T: " + this.minecraft.level.getEntityCount();
            var32[6] = this.minecraft.level.gatherChunkSourceStats();
            var32[7] = "";
            var32[8] = String.format(Locale.ROOT, "Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
            return Lists.newArrayList(var32);
        } else {
            Entity entity = this.minecraft.getCameraEntity();
            Direction direction = entity.getDirection();
            String var10000;
            switch (direction) {
                case NORTH -> var10000 = "Towards negative Z";
                case SOUTH -> var10000 = "Towards positive Z";
                case WEST -> var10000 = "Towards negative X";
                case EAST -> var10000 = "Towards positive X";
                default -> var10000 = "Invalid";
            }

            String $$21 = var10000;
            ChunkPos chunkpos = new ChunkPos(blockpos);
            if (!Objects.equals(this.lastPos, chunkpos)) {
                this.lastPos = chunkpos;
                this.clearChunkCache();
            }

            Level level = this.getLevel();
            LongSet longset = level instanceof ServerLevel ? ((ServerLevel)level).getForcedChunks() : LongSets.EMPTY_SET;
            var32 = new String[7];
            var10003 = SharedConstants.getCurrentVersion().getName();
            var32[0] = "Minecraft " + var10003 + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) + ")";
            var32[1] = this.minecraft.fpsString;
            var32[2] = s;
            var32[3] = this.minecraft.levelRenderer.getSectionStatistics();
            var32[4] = this.minecraft.levelRenderer.getEntityStatistics();
            var10003 = this.minecraft.particleEngine.countParticles();
            var32[5] = "P: " + var10003 + ". T: " + this.minecraft.level.getEntityCount();
            var32[6] = this.minecraft.level.gatherChunkSourceStats();
            List<String> list = Lists.newArrayList(var32);
            String s4 = this.getServerChunkStats();
            if (s4 != null) {
                list.add(s4);
            }

            String var10001 = String.valueOf(this.minecraft.level.dimension().location());
            list.add(var10001 + " FC: " + ((LongSet)longset).size());
            list.add("");
            list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.minecraft.getCameraEntity().getX(), this.minecraft.getCameraEntity().getY(), this.minecraft.getCameraEntity().getZ()));
            list.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
            list.add(String.format(Locale.ROOT, "Block Long: %d", blockpos.asLong()));
            list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", chunkpos.x, SectionPos.blockToSectionCoord(blockpos.getY()), chunkpos.z, chunkpos.getRegionLocalX(), chunkpos.getRegionLocalZ(), chunkpos.getRegionX(), chunkpos.getRegionZ()));
            list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, $$21, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot())));
            LevelChunk levelchunk = this.getClientChunk();
            if (levelchunk.isEmpty()) {
                list.add("Waiting for chunk...");
            } else {
                int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
                int j = this.minecraft.level.getBrightness(LightLayer.SKY, blockpos);
                int k = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockpos);
                list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
                LevelChunk levelchunk1 = this.getServerChunk();
                StringBuilder stringbuilder = new StringBuilder("CH");
                Heightmap.Types[] var24 = Heightmap.Types.values();
                int var25 = var24.length;

                int var26;
                Heightmap.Types heightmap$types1;
                for(var26 = 0; var26 < var25; ++var26) {
                    heightmap$types1 = var24[var26];
                    if (heightmap$types1.sendToClient()) {
                        stringbuilder.append(" ").append((String)HEIGHTMAP_NAMES.get(heightmap$types1)).append(": ").append(levelchunk.getHeight(heightmap$types1, blockpos.getX(), blockpos.getZ()));
                    }
                }

                list.add(stringbuilder.toString());
                stringbuilder.setLength(0);
                stringbuilder.append("SH");
                var24 = Heightmap.Types.values();
                var25 = var24.length;

                for(var26 = 0; var26 < var25; ++var26) {
                    heightmap$types1 = var24[var26];
                    if (heightmap$types1.keepAfterWorldgen()) {
                        stringbuilder.append(" ").append((String)HEIGHTMAP_NAMES.get(heightmap$types1)).append(": ");
                        if (levelchunk1 != null) {
                            stringbuilder.append(levelchunk1.getHeight(heightmap$types1, blockpos.getX(), blockpos.getZ()));
                        } else {
                            stringbuilder.append("??");
                        }
                    }
                }

                list.add(stringbuilder.toString());
                if (blockpos.getY() >= this.minecraft.level.getMinBuildHeight() && blockpos.getY() < this.minecraft.level.getMaxBuildHeight()) {
                    Holder var31 = this.minecraft.level.getBiome(blockpos);
                    list.add("Biome: " + printBiome(var31));
                    if (levelchunk1 != null) {
                        float f2 = level.getMoonBrightness();
                        long l = levelchunk1.getInhabitedTime();
                        DifficultyInstance difficultyinstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), l, f2);
                        list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", difficultyinstance.getEffectiveDifficulty(), difficultyinstance.getSpecialMultiplier(), this.minecraft.level.getDayTime() / 24000L));
                    } else {
                        list.add("Local Difficulty: ??");
                    }
                }

                if (levelchunk1 != null && levelchunk1.isOldNoiseGeneration()) {
                    list.add("Blending: Old");
                }
            }

            ServerLevel serverlevel = this.getServerLevel();
            if (serverlevel != null) {
                ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
                ChunkGenerator chunkgenerator = serverchunkcache.getGenerator();
                RandomState randomstate = serverchunkcache.randomState();
                chunkgenerator.addDebugScreenInfo(list, randomstate, blockpos);
                Climate.Sampler climate$sampler = randomstate.sampler();
                BiomeSource biomesource = chunkgenerator.getBiomeSource();
                biomesource.addDebugInfo(list, blockpos, climate$sampler);
                NaturalSpawner.SpawnState naturalspawner$spawnstate = serverchunkcache.getLastSpawnState();
                if (naturalspawner$spawnstate != null) {
                    Object2IntMap<MobCategory> object2intmap = naturalspawner$spawnstate.getMobCategoryCounts();
                    int i1 = naturalspawner$spawnstate.getSpawnableChunkCount();
                    list.add("SC: " + i1 + ", " + (String) Stream.of(MobCategory.values()).map((p_94068_) -> {
                        char var10002 = Character.toUpperCase(p_94068_.getName().charAt(0));
                        return "" + var10002 + ": " + object2intmap.getInt(p_94068_);
                    }).collect(Collectors.joining(", ")));
                } else {
                    list.add("SC: N/A");
                }
            }

            PostChain postchain = this.minecraft.gameRenderer.currentEffect();
            if (postchain != null) {
                list.add("Shader: " + postchain.getName());
            }

            var10001 = this.minecraft.getSoundManager().getDebugString();
            list.add(var10001 + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F)));
            return list;
        }
    }

    @Shadow
    protected abstract ServerLevel getServerLevel();

    @Shadow
    private static String printBiome(Holder<Biome> biomeHolder) {
        return null;
    }

    @Shadow
    protected abstract LevelChunk getServerChunk();

    @Shadow
    protected abstract LevelChunk getClientChunk();

    @Shadow
    protected abstract String getServerChunkStats();

    @Shadow
    public abstract void clearChunkCache();

    @Shadow
    protected abstract Level getLevel();
}
