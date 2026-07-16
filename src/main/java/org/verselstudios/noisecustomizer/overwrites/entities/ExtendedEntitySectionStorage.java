package org.verselstudios.noisecustomizer.overwrites.entities;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.verselstudios.noisecustomizer.utils.CoordinateKey;
import org.verselstudios.noisecustomizer.utils.Entry;
import org.verselstudios.noisecustomizer.utils.TreeCoordinateStorage;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

public class ExtendedEntitySectionStorage<T extends EntityAccess> {

    private final Class<T> entityClass;

    /**
     * Still chunk based for now.
     * This can later become Function<ChunkKey, Visibility>
     */
    private final Long2ObjectMap<Visibility> initialSectionVisibility;

    private final TreeCoordinateStorage<EntitySection<T>> sections =
            new TreeCoordinateStorage<>();


    public ExtendedEntitySectionStorage(
            Class<T> entityClass,
            Long2ObjectMap<Visibility> initialSectionVisibility
    ) {
        this.entityClass = entityClass;
        this.initialSectionVisibility = initialSectionVisibility;
    }


    public void forEachAccessibleNonEmptySection(
            AABB boundingBox,
            AbortableIterationConsumer<EntitySection<T>> consumer
    ) {
        int minX = SectionPos.posToSectionCoord(boundingBox.minX - 2.0);
        int minY = SectionPos.posToSectionCoord(boundingBox.minY - 4.0);
        int minZ = SectionPos.posToSectionCoord(boundingBox.minZ - 2.0);

        int maxX = SectionPos.posToSectionCoord(boundingBox.maxX + 2.0);
        int maxY = SectionPos.posToSectionCoord(boundingBox.maxY);
        int maxZ = SectionPos.posToSectionCoord(boundingBox.maxZ + 2.0);


        for (Entry<EntitySection<T>> entry :
                sections.range(
                        minX, minY, minZ,
                        maxX, maxY, maxZ
                )) {

            EntitySection<T> section = entry.value();

            if (!section.isEmpty() && section.getStatus() != null
                    && section.getStatus().isAccessible()
                    && consumer.accept(section).shouldAbort()) {
                return;
            }
        }
    }


    public Stream<CoordinateKey> getExistingSectionPositionsInChunk(long pos) {
        int x = ChunkPos.getX(pos);
        int z = ChunkPos.getZ(pos);

        return sections.streamRange(
                        x,
                        Integer.MIN_VALUE,
                        z,
                        x,
                        Integer.MAX_VALUE,
                        z
                )
                .map(Entry::key);
    }


    public Stream<EntitySection<T>> getExistingSectionsInChunk(long pos) {
        return getExistingSectionPositionsInChunk(pos)
                .map(sections::get)
                .filter(Objects::nonNull);
    }


    private static long getChunkKeyFromSectionKey(CoordinateKey pos) {
        return ChunkPos.asLong(pos.x(), pos.z());
    }


    public EntitySection<T> getOrCreateSection(CoordinateKey sectionPos) {
        EntitySection<T> existing = sections.get(sectionPos);

        if (existing != null) {
            return existing;
        }

        Visibility visibility =
                initialSectionVisibility.get(
                        ChunkPos.asLong(sectionPos.x(), sectionPos.z())
                );

        EntitySection<T> section =
                new EntitySection<>(
                        entityClass,
                        visibility
                );

        sections.put(sectionPos, section);

        return section;
    }


    @Nullable
    public EntitySection<T> getSection(CoordinateKey key) {
        return sections.get(key);
    }


    public LongSet getAllChunksWithExistingSections() {
        LongSet chunks = new LongOpenHashSet();

        for (CoordinateKey key : sections.keys()) {
            chunks.add(
                    getChunkKeyFromSectionKey(key)
            );
        }

        return chunks;
    }


    public void getEntities(
            AABB bounds,
            AbortableIterationConsumer<T> consumer
    ) {
        forEachAccessibleNonEmptySection(
                bounds,
                section -> section.getEntities(bounds, consumer)
        );
    }


    public <U extends T> void getEntities(
            EntityTypeTest<T, U> test,
            AABB bounds,
            AbortableIterationConsumer<U> consumer
    ) {
        forEachAccessibleNonEmptySection(
                bounds,
                section -> section.getEntities(test, bounds, consumer)
        );
    }


    public void remove(CoordinateKey key) {
        sections.remove(key);
    }


    @VisibleForDebug
    public int count() {
        return sections.size();
    }
}