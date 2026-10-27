package com.susu609.modularplayer.appearance;

/**
 * Independent appearance measurements. General axes are relative to Steve.
 * Anatomy offsets are model pixels, optional soft motion uses 0..1.
 * These are cosmetic only: never change collision, eye height or reach.
 */
public enum BodyDimension {
    HEAD_WIDTH(Group.HEAD, "Head width", 1.0, 0.65, 1.60),
    HEAD_HEIGHT(Group.HEAD, "Head height", 1.0, 0.65, 1.55),
    HEAD_DEPTH(Group.HEAD, "Head depth", 1.0, 0.65, 1.60),

    TORSO_WIDTH(Group.TORSO, "Torso width", 1.0, 0.60, 1.65),
    TORSO_HEIGHT(Group.TORSO, "Torso length", 1.0, 0.70, 1.45),
    TORSO_DEPTH(Group.TORSO, "Torso depth", 1.0, 0.65, 1.60),

    ARM_WIDTH(Group.ARMS, "Arm width", 1.0, 0.60, 1.65),
    ARM_LENGTH(Group.ARMS, "Arm length", 1.0, 0.70, 1.45),
    ARM_DEPTH(Group.ARMS, "Arm depth", 1.0, 0.65, 1.60),

    LEG_WIDTH(Group.LEGS, "Leg width", 1.0, 0.60, 1.65),
    LEG_LENGTH(Group.LEGS, "Leg length", 1.0, 0.70, 1.45),
    LEG_DEPTH(Group.LEGS, "Leg depth", 1.0, 0.65, 1.60),

    CHEST_VOLUME(Group.CHEST, "Projection", 0.0, 0.0, 1.8),
    CHEST_SEPARATION(Group.CHEST, "Separation", 0.0, 0.0, 2.0),
    CHEST_POSITION(Group.CHEST, "Vertical offset", 0.0, -2.0, 2.0),
    CHEST_WIDTH(Group.CHEST, "Width", 1.0, 0.65, 1.55),
    CHEST_HEIGHT(Group.CHEST, "Height", 1.0, 0.70, 1.40),
    CHEST_SWAY(Group.CHEST, "Soft motion", 0.0, 0.0, 1.0),

    HIP_WIDTH(Group.HIPS, "Hip width", 1.0, 0.70, 1.60),

    BUTTOCK_VOLUME(Group.BUTTOCKS, "Projection", 0.0, 0.0, 1.8),
    BUTTOCK_SEPARATION(Group.BUTTOCKS, "Separation", 0.0, 0.0, 2.0),
    BUTTOCK_POSITION(Group.BUTTOCKS, "Vertical offset", 0.0, -1.5, 1.5),
    BUTTOCK_WIDTH(Group.BUTTOCKS, "Width", 1.0, 0.65, 1.60),
    BUTTOCK_HEIGHT(Group.BUTTOCKS, "Height", 1.0, 0.70, 1.40);

    public enum Group { HEAD, TORSO, ARMS, LEGS, CHEST, HIPS, BUTTOCKS, OUTFIT }

    private final Group group;
    private final String label;
    private final double standard;
    private final double min;
    private final double max;

    BodyDimension(Group group, String label, double standard, double min, double max) {
        this.group = group;
        this.label = label;
        this.standard = standard;
        this.min = min;
        this.max = max;
    }

    public Group group() { return group; }
    public String label() { return label; }
    public double standard() { return standard; }
    public double min() { return min; }
    public double max() { return max; }

    public double clamp(double number) {
        return Math.max(min, Math.min(max, number));
    }

    public String configName() { return name().toLowerCase(java.util.Locale.ROOT); }

    /** Slider labels distinguish physical offsets, factors, and motion strength. */
    public String format(double value) {
        if (name().endsWith("_SEPARATION") || name().endsWith("_POSITION")) {
            return String.format(java.util.Locale.ROOT, "%.1f px", value);
        }
        if (this == CHEST_SWAY) return Math.round(value * 100) + "%";
        return String.format(java.util.Locale.ROOT, "%.2fx", value);
    }
}
