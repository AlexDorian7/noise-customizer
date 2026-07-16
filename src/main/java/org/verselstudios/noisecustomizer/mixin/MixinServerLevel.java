package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.verselstudios.noisecustomizer.Config;
import org.verselstudios.noisecustomizer.overwrites.entities.ExtendedPersistentEntitySectionManager;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    static {
        System.out.println("LOADING SERVER LEVEL MIXIN");
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/level/entity/PersistentEntitySectionManager"
            )
    )
    private <T extends EntityAccess> PersistentEntitySectionManager<T> replaceEntityManager(Class<T> entityClass, net.minecraft.world.level.entity.LevelCallback<T> callbacks, EntityPersistentStorage<T> storage) {
        System.out.println("RUNNING SERVER LEVEL MIXIN");
        if (Config.FIX_SECTIONS.get()) {
            return new ExtendedPersistentEntitySectionManager<>(entityClass, callbacks, storage);
        } else {
            return new PersistentEntitySectionManager<>(entityClass, callbacks, storage);
        }
    }
}

