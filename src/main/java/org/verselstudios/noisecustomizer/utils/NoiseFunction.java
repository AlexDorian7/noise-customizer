package org.verselstudios.noisecustomizer.utils;

@FunctionalInterface
public interface NoiseFunction {
    double noise(double x, double y, double z);
}