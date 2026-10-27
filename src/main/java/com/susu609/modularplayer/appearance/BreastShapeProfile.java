package com.susu609.modularplayer.appearance;

/**
 * Original parametric body-shape functions: no external model/texture dependency.
 * Profiles interpolate the silhouette; their labels are artistic references,
 * not claims of compatibility with third-party model data.
 */
public enum BreastShapeProfile {
    JENNY_INSPIRED("Jenny-inspired"),
    NATURAL("Natural"),
    ROUND("Round"),
    ANGULAR("Angular");

    private final String label;

    BreastShapeProfile(String label) { this.label = label; }

    public String label() { return label; }

    public static BreastShapeProfile fromIndex(double sliderIndex) {
        int index = Math.max(0, Math.min(values().length - 1,
                (int) Math.round(sliderIndex)));
        return values()[index];
    }

    /**
     * Normalized front surface height. Ranges 0..1; exactly 0 at outer rim.
     * v > 0 is anatomically lower in Minecraft model coordinates.
     * Fullness 0..1 changes lower contour independent of overall projection.
     */
    public float depth(float u, float v, float fullness) {
        float radial = Math.max(0F, 1F - u * u - v * v);
        float lower = Math.max(0F, v);
        float upper = Math.max(0F, -v);
        float biased = (1F + .50F * fullness * lower - .12F * (1F - fullness) * upper);
        float dome = switch (this) {
            case JENNY_INSPIRED -> (float) Math.pow(radial, 1.02) * (1F + .12F * lower);
            case NATURAL -> (float) Math.pow(radial, 1.30) * (1F + .20F * lower);
            case ROUND -> (float) Math.pow(radial, .70);
            case ANGULAR -> (float) Math.pow(radial, .38);
        };
        return Math.max(0F, Math.min(1.65F, dome * biased));
    }
}
