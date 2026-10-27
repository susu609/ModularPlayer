package com.susu609.modularplayer;

import com.susu609.modularplayer.appearance.Appearance;
import net.minecraftforge.common.ForgeConfigSpec;

/** This version is visual and local-player-only; not a hitbox/camera mod. */
public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.EnumValue<Appearance.Shape> HEAD, TORSO, ARMS, LEGS;
    public static final ForgeConfigSpec.EnumValue<Appearance.Headwear> HEADWEAR;
    public static final ForgeConfigSpec.EnumValue<Appearance.Top> TOP;
    public static final ForgeConfigSpec.EnumValue<Appearance.Bottom> BOTTOM;
    public static final ForgeConfigSpec.EnumValue<Appearance.Footwear> FEET;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("appearance");
        ENABLED = b.define("enabled", true);
        b.push("body");
        HEAD = b.defineEnum("head", Appearance.Shape.STEVE);
        TORSO = b.defineEnum("torso", Appearance.Shape.STEVE);
        ARMS = b.defineEnum("arms", Appearance.Shape.STEVE);
        LEGS = b.defineEnum("legs", Appearance.Shape.STEVE);
        b.pop();
        b.push("outfit");
        HEADWEAR = b.defineEnum("headwear", Appearance.Headwear.NONE);
        TOP = b.defineEnum("top", Appearance.Top.STEVE_BLUE);
        BOTTOM = b.defineEnum("bottom", Appearance.Bottom.STEVE_PANTS);
        FEET = b.defineEnum("feet", Appearance.Footwear.NONE);
        b.pop();
        b.pop();
        SPEC = b.build();
    }

    private ClientConfig() {}

    public static Appearance readAppearance() {
        return new Appearance(HEAD.get(), TORSO.get(), ARMS.get(), LEGS.get(),
                HEADWEAR.get(), TOP.get(), BOTTOM.get(), FEET.get());
    }
}
