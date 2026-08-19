package org.verselstudios.noisecustomizer.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.verselstudios.noisecustomizer.NoiseCustomizer;
import org.verselstudios.noisecustomizer.utils.NoiseFunction;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class LuaState {
    private static final LuaState INSTANCE = new LuaState("config/noisecustomizer/noise.lua");

    private final Globals globals = JsePlatform.standardGlobals();
    private LuaValue blendedNoise = null;
    private LuaValue normalNoise = null;
    private NoiseFunction noiseFunction = (x, y, z) -> 0;

    private LuaState(String file) {
        LuaNoiseBridge.registerNoise(globals, (x, y, z) -> noiseFunction.noise(x, y, z));
        LuaValue chunk;
        try {
            chunk = globals.load(new FileReader(file), "noise.lua");
            chunk.call();
            blendedNoise = globals.get("blendedNoise");
            normalNoise = globals.get("normalNoise");

            if (!blendedNoise.isfunction()) {
                NoiseCustomizer.LOGGER.warn("Lua script is missing blendedNoise()");
                blendedNoise = null;
            }
            if (!normalNoise.isfunction()) {
                NoiseCustomizer.LOGGER.warn("Lua script is missing normalNoise()");
                normalNoise = null;
            }
        } catch (FileNotFoundException e) {
            NoiseCustomizer.LOGGER.warn("Lua File not found at \"config/noisecustomizer/noise.lua\"");
        }

    }

    public double blendedNoise(double x, double y, double z) {
        if (blendedNoise == null) return 0;
        LuaValue result = blendedNoise.call(
                LuaValue.valueOf(x),
                LuaValue.valueOf(y),
                LuaValue.valueOf(z)
        );

        return result.todouble();
    }

    public double normalNoise(double x, double y, double z) {
        if (normalNoise == null) return 0;
        LuaValue result = normalNoise.call(
                LuaValue.valueOf(x),
                LuaValue.valueOf(y),
                LuaValue.valueOf(z)
        );

        return result.todouble();
    }

    public static LuaState getDefaultInstance() {
        return INSTANCE;
    }

    public NoiseFunction getNoiseFunction() {
        return noiseFunction;
    }

    public void setNoiseFunction(NoiseFunction noiseFunction) {
        this.noiseFunction = noiseFunction;
    }
}
