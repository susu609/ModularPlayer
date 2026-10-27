package com.susu609.modularplayer.appearance;

/**
 * Independent appearance dimensions. A value of 1 is the Steve baseline.
 * All values are visual only and never alter collision, eye height or reach.
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

    CHEST_VOLUME(Group.SHAPE, "Chest", 0.0, 0.0, 1.8),
    BUTTOCK_VOLUME(Group.SHAPE, "Buttocks", 0.0, 0.0, 1.8),
    HIP_WIDTH(Group.SHAPE, "Hips", 1.0, 0.70, 1.60);

    public enum Group { HEAD, TORSO, ARMS, LEGS, SHAPE, OUTFIT }

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

    /** Sliders show actual pixel offsets rather than misleading percentages. */
    public String format(double value) {
        if (name().endsWith("_SEPARATION") || name().endsWith("_POSITION")) {
            return String.format(java.util.Locale.ROOT, "%.1f px", value);
        }
        if (this == CHEST_SWAY) {
            return Math.round(value * 100) + "%";
        }
        return String.format(java.util.Locale.ROOT, "%.2fx", value);
    }
}
