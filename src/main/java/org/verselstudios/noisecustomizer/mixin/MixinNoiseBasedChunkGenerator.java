package org.verselstudios.noisecustomizer.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.text.DecimalFormat;
import java.util.List;

@Mixin(NoiseBasedChunkGenerator.class)
public class MixinNoiseBasedChunkGenerator {

    /**
     * @author Versel
     * @reason Add more debug info
     */
    @Overwrite
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        DecimalFormat decimalformat = new DecimalFormat("0.00000");
        NoiseRouter noiserouter = random.router();
        DensityFunction.SinglePointContext densityfunction$singlepointcontext = new DensityFunction.SinglePointContext(
                pos.getX(), pos.getY(), pos.getZ()
        );
        double d0 = noiserouter.ridges().compute(densityfunction$singlepointcontext);

        // Add cords
        info.add("NoiseRouter Pos §4X:" + pos.getX() + " §2Y: " + pos.getY() + " §1Z: " + pos.getZ() + "§r");

        info.add(
                "NoiseRouter §cT: "
                        + decimalformat.format(noiserouter.temperature().compute(densityfunction$singlepointcontext))
                        + " §aV: "
                        + decimalformat.format(noiserouter.vegetation().compute(densityfunction$singlepointcontext))
                        + " §bC: "
                        + decimalformat.format(noiserouter.continents().compute(densityfunction$singlepointcontext))
                        + " §7E: "
                        + decimalformat.format(noiserouter.erosion().compute(densityfunction$singlepointcontext))
                        + " §1D: "
                        + decimalformat.format(noiserouter.depth().compute(densityfunction$singlepointcontext))
                        + " §dW: "
                        + decimalformat.format(d0)
                        + " §fPV: "
                        + decimalformat.format(NoiseRouterData.peaksAndValleys((float)d0))
                        + " §eAS: "
                        + decimalformat.format(noiserouter.initialDensityWithoutJaggedness().compute(densityfunction$singlepointcontext))
                        + " §6N: "
                        + decimalformat.format(noiserouter.finalDensity().compute(densityfunction$singlepointcontext))
                        + "§r"
        );
    }
}
