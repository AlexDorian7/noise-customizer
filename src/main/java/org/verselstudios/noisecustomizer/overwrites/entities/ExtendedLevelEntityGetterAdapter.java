package org.verselstudios.noisecustomizer.overwrites.entities;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;

public class ExtendedLevelEntityGetterAdapter<T extends EntityAccess> extends LevelEntityGetterAdapter<T> {
    private final ExtendedEntitySectionStorage<T> sectionStorage;

    public ExtendedLevelEntityGetterAdapter(EntityLookup visibleEntities, ExtendedEntitySectionStorage<T> sectionStorage) {
        super(visibleEntities, null); // I know what I am doing. I have overridden both methods that would use this null value
        this.sectionStorage = sectionStorage;
    }

    @Override
    public void get(AABB boundingBox, Consumer<T> consumer) {
        this.sectionStorage.getEntities(boundingBox, AbortableIterationConsumer.forConsumer(consumer));
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> test, AABB bounds, AbortableIterationConsumer<U> consumer) {
        this.sectionStorage.getEntities(test, bounds, consumer);
    }
}
