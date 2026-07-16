package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.verselstudios.noisecustomizer.overwrites.entities.ExtendedTransientEntitySectionManager;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/level/entity/TransientEntitySectionManager"
            )
    )
    private <T extends EntityAccess> TransientEntitySectionManager<T> replaceEntityStorage(
            Class<T> entityClass,
            LevelCallback<T> callback
    ) {
        System.out.println("USING EXTENDED CLIENT ENTITY STORAGE");

        return new ExtendedTransientEntitySectionManager<>(
                entityClass,
                callback
        );
    }

}
