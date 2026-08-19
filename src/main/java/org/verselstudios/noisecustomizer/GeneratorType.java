package org.verselstudios.noisecustomizer;

import net.minecraft.util.StringRepresentable;

public enum GeneratorType implements StringRepresentable {
    DEFAULT("Default"),
    LUA("Lua"),
    ZERO("Zero"),
    ONE("One");


    private final String name;

    GeneratorType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
