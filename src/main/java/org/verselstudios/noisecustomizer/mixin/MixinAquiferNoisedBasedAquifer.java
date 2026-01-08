package org.verselstudios.noisecustomizer.mixin;

import ca.weblite.objc.foundation.NSRange;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Aquifer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Aquifer.NoiseBasedAquifer.class)
public class MixinAquiferNoisedBasedAquifer {

    @Shadow
    @Final
    protected int minGridX;

    @Shadow
    @Final
    protected int minGridY;

    @Shadow
    @Final
    protected int minGridZ;

    @Shadow
    @Final
    protected int gridSizeZ;

    @Shadow
    @Final
    protected int gridSizeX;

    @Shadow
    @Final
    protected Aquifer.FluidStatus[] aquiferCache;

    /**
     * @author Versel
     * @reason To remove 30 mil border
     */
    @Overwrite
    protected int getIndex(int gridX, int gridY, int gridZ) {
        int i = gridX - this.minGridX;
        int j = gridY - this.minGridY;
        int k = gridZ - this.minGridZ;
        int di = (j * this.gridSizeZ + k) * this.gridSizeX + i;
        return Mth.clamp(di, 0, aquiferCache.length - 1);
    }
}
