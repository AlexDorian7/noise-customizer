package org.verselstudios.noisecustomizer.lua;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.verselstudios.noisecustomizer.utils.NoiseFunction;

public class LuaNoiseBridge {

    public static void registerNoise(Globals globals, NoiseFunction noise) {
        globals.set("noise", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue x, LuaValue y, LuaValue z) {
                double result = noise.noise(
                        x.checkdouble(),
                        y.checkdouble(),
                        z.checkdouble()
                );

                return LuaDouble.valueOf(result);
            }
        });
    }
}