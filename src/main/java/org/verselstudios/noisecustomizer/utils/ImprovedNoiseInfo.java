package org.verselstudios.noisecustomizer.utils;

public record ImprovedNoiseInfo(
        int octave,
        double amplitude,
        double coordinateScale,
        double xo,
        double yo,
        double zo
) {}