package org.verselstudios.noisecustomizer.mixin;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntitySectionStorage.class)
public class EntitySectionStorageMixin<T extends EntityAccess> {

    @Shadow
    @Final
    private Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap<>();

    @Shadow
    @Final
    private LongSortedSet sectionIds = new LongAVLTreeSet();

    /**
     * @author Versel
     * @reason Fix entities crashing at 2^25
     */
    @Overwrite
    public void forEachAccessibleNonEmptySection(AABB boundingBox, AbortableIterationConsumer<EntitySection<T>> consumer) {
        int i = 2;
        int j = SectionPos.posToSectionCoord(boundingBox.minX - 2.0);
        int k = SectionPos.posToSectionCoord(boundingBox.minY - 4.0);
        int l = SectionPos.posToSectionCoord(boundingBox.minZ - 2.0);
        int i1 = SectionPos.posToSectionCoord(boundingBox.maxX + 2.0);
        int j1 = SectionPos.posToSectionCoord(boundingBox.maxY + 0.0);
        int k1 = SectionPos.posToSectionCoord(boundingBox.maxZ + 2.0);

        for (int l1 = j; l1 <= i1; l1++) {
            long i2 = SectionPos.asLong(l1, 0, 0);
            long j2 = SectionPos.asLong(l1, -1, -1);
            LongIterator longiterator = this.sectionIds.subSet(i2, j2 + 1L).iterator();

            while (longiterator.hasNext()) {
                long k2 = longiterator.nextLong();
                int l2 = SectionPos.y(k2);
                int i3 = SectionPos.z(k2);
                if (l2 >= k && l2 <= j1 && i3 >= l && i3 <= k1) {
                    EntitySection<T> entitysection = this.sections.get(k2);
                    if (entitysection != null
                            && !entitysection.isEmpty()
                            && entitysection.getStatus().isAccessible()
                            && consumer.accept(entitysection).shouldAbort()) {
                        return;
                    }
                }
            }
        }
    }
}
