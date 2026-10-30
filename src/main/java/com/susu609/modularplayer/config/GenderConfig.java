package com.susu609.modularplayer.config;

import com.susu609.modularplayer.gender.Gender;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Temporary per-client settings. These are intentionally not synchronized:
 * the foundation only renders the local player's appearance.
 */
public final class GenderConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.EnumValue<Gender> GENDER;
    public static final ForgeConfigSpec.DoubleValue BUST_SIZE;
    public static final ForgeConfigSpec.DoubleValue WIDTH;
    public static final ForgeConfigSpec.DoubleValue HEIGHT;
    public static final ForgeConfigSpec.DoubleValue DEPTH;
    public static final ForgeConfigSpec.DoubleValue SEPARATION;
    public static final ForgeConfigSpec.DoubleValue VERTICAL_OFFSET;
    public static final ForgeConfigSpec.BooleanValue PHYSICS;
    public static final ForgeConfigSpec.DoubleValue BOUNCE;
    public static final ForgeConfigSpec.DoubleValue STIFFNESS;
    public static final ForgeConfigSpec.DoubleValue DAMPING;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("gender");
        ENABLED = b.comment("Enable the gender render layer for the local player.")
                .define("enabled", true);
        GENDER = b.defineEnum("selection", Gender.FEMALE);
        b.push("breasts");
        BUST_SIZE = b.defineInRange("size", .65D, 0D, 1.80D);
        WIDTH = b.defineInRange("width", 1D, .50D, 1.80D);
        HEIGHT = b.defineInRange("height", 1D, .50D, 1.80D);
        DEPTH = b.defineInRange("depth", 1D, .50D, 2.00D);
        SEPARATION = b.defineInRange("separation", 0D, -1.5D, 3D);
        VERTICAL_OFFSET = b.defineInRange("vertical_offset", 0D, -3D, 3D);
        b.pop();
        b.push("physics");
        PHYSICS = b.define("enabled", true);
        BOUNCE = b.defineInRange("strength", .55D, 0D, 2D);
        STIFFNESS = b.defineInRange("stiffness", .55D, .05D, 1D);
        DAMPING = b.defineInRange("damping", .62D, .05D, 1D);
        b.pop();
        b.pop();
        SPEC = b.build();
    }

    private GenderConfig() {}
    public static boolean visible() {
        return ENABLED.get() && GENDER.get() == Gender.FEMALE && BUST_SIZE.get() > .001D;
    }
}
