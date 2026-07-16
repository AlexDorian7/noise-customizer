package org.verselstudios.noisecustomizer.overwrites.entities;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.*;
import org.slf4j.Logger;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;

public class ExtendedTransientEntitySectionManager<T extends EntityAccess> extends TransientEntitySectionManager<T> {
    static final Logger LOGGER = LogUtils.getLogger();
    final LevelCallback<T> callbacks;
    final EntityLookup<T> entityStorage;
    final ExtendedEntitySectionStorage<T> sectionStorage;
    private final Long2ObjectMap<Visibility> tickingChunks = new Long2ObjectOpenHashMap<>();
    private final LevelEntityGetter<T> entityGetter;

    public ExtendedTransientEntitySectionManager(Class<T> clazz, LevelCallback<T> callbacks) {
        super(clazz, callbacks);
        tickingChunks.defaultReturnValue(Visibility.HIDDEN);
        this.entityStorage = new EntityLookup<>();
        this.sectionStorage = new ExtendedEntitySectionStorage<>(
                clazz, tickingChunks
        );
        this.callbacks = callbacks;
        this.entityGetter = new ExtendedLevelEntityGetterAdapter<>(this.entityStorage, this.sectionStorage);
    }

    public void startTicking(ChunkPos pos) {
        long i = pos.toLong();
        this.tickingChunks.put(i, Visibility.TICKING);
        this.sectionStorage.getExistingSectionsInChunk(i).forEach(p_157663_ -> {
            Visibility visibility = p_157663_.updateChunkStatus(Visibility.TICKING);
            if (!visibility.isTicking()) {
                p_157663_.getEntities().filter(p_157666_ -> !p_157666_.isAlwaysTicking()).forEach(this.callbacks::onTickingStart);
            }
        });
    }

    public void stopTicking(ChunkPos pos) {
        long i = pos.toLong();
        this.tickingChunks.remove(i);
        this.sectionStorage.getExistingSectionsInChunk(i).forEach(p_157656_ -> {
            Visibility visibility = p_157656_.updateChunkStatus(Visibility.TRACKED);
            if (visibility.isTicking()) {
                p_157656_.getEntities().filter(p_157661_ -> !p_157661_.isAlwaysTicking()).forEach(this.callbacks::onTickingEnd);
            }
        });
    }

    public LevelEntityGetter<T> getEntityGetter() {
        return this.entityGetter;
    }

    public void addEntity(T entity) {
        this.entityStorage.add(entity);

        BlockPos pos = entity.blockPosition();

        CoordinateKey sectionPos = new CoordinateKey(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getY()),
                SectionPos.blockToSectionCoord(pos.getZ())
        );

        EntitySection<T> section =
                this.sectionStorage.getOrCreateSection(sectionPos);

        section.add(entity);

        entity.setLevelCallback(
                new ExtendedTransientEntitySectionManager.Callback(
                        entity,
                        sectionPos,
                        section
                )
        );

        this.callbacks.onCreated(entity);

        Visibility visibility =
                ExtendedPersistentEntitySectionManager.getEffectiveStatus(
                        entity,
                        section.getStatus()
                );

        if (visibility == null)
            visibility = Visibility.HIDDEN;

        if (visibility.isAccessible()) {
            this.callbacks.onTrackingStart(entity);
        }

        if (visibility.isTicking()) {
            this.callbacks.onTickingStart(entity);
        }
    }

    @VisibleForDebug
    public int count() {
        return this.entityStorage.count();
    }

    void removeSectionIfEmpty(CoordinateKey section, EntitySection<T> entitySection) {
        if (entitySection.isEmpty()) {
            this.sectionStorage.remove(section);
        }
    }

    @VisibleForDebug
    public String gatherStats() {
        return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
    }

    class Callback implements EntityInLevelCallback {
        private final T entity;
        private final Entity realEntity;
        private CoordinateKey currentSectionKey;
        private EntitySection<T> currentSection;

        Callback(T entity, CoordinateKey section, EntitySection<T> currentSection) {
            this.entity = entity;
            this.realEntity = entity instanceof Entity ? (Entity) entity : null;
            this.currentSectionKey = section;
            this.currentSection = currentSection;
        }

        @Override
        public void onMove() {
            BlockPos blockpos = this.entity.blockPosition();

            CoordinateKey newSectionKey = new CoordinateKey(
                    SectionPos.blockToSectionCoord(blockpos.getX()),
                    SectionPos.blockToSectionCoord(blockpos.getY()),
                    SectionPos.blockToSectionCoord(blockpos.getZ())
            );
            if (newSectionKey != this.currentSectionKey) {
                Visibility visibility = this.currentSection.getStatus();
                if (!this.currentSection.remove(this.entity)) {
                    ExtendedTransientEntitySectionManager.LOGGER
                            .warn("Entity {} wasn't found in section {} (moving to {})", this.entity, this.currentSectionKey, newSectionKey);
                }

                ExtendedTransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
                EntitySection<T> entitysection = ExtendedTransientEntitySectionManager.this.sectionStorage.getOrCreateSection(newSectionKey);
                entitysection.add(this.entity);
                CoordinateKey oldSectionKey = currentSectionKey;
                this.currentSection = entitysection;
                this.currentSectionKey = newSectionKey;
                ExtendedTransientEntitySectionManager.this.callbacks.onSectionChange(this.entity);
                if (!this.entity.isAlwaysTicking()) {
                    boolean flag = visibility.isTicking();
                    boolean flag1 = entitysection.getStatus().isTicking();
                    if (flag && !flag1) {
                        ExtendedTransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
                    } else if (!flag && flag1) {
                        ExtendedTransientEntitySectionManager.this.callbacks.onTickingStart(this.entity);
                    }
                }
//                if (this.realEntity != null) net.neoforged.neoforge.common.CommonHooks.onEntityEnterSection(this.realEntity, oldSectionKey, newSectionKey); // Disable event
            }
        }

        @Override
        public void onRemove(Entity.RemovalReason reason) {
            if (!this.currentSection.remove(this.entity)) {
                ExtendedTransientEntitySectionManager.LOGGER
                        .warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, this.currentSectionKey, reason);
            }

            Visibility visibility = this.currentSection.getStatus();
            if (visibility.isTicking() || this.entity.isAlwaysTicking()) {
                ExtendedTransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
            }

            ExtendedTransientEntitySectionManager.this.callbacks.onTrackingEnd(this.entity);
            ExtendedTransientEntitySectionManager.this.callbacks.onDestroyed(this.entity);
            ExtendedTransientEntitySectionManager.this.entityStorage.remove(this.entity);
            this.entity.setLevelCallback(NULL);
            ExtendedTransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
        }
    }
}