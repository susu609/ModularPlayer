package com.susu609.modularplayer;

import com.susu609.modularplayer.appearance.Appearance;
import com.susu609.modularplayer.appearance.BodyDimension;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.EnumMap;
import java.util.Map;

/** Client-only preferences. This is an appearance editor, not a gameplay size mod. */
public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.EnumValue<Appearance.Shape> HEAD, TORSO, ARMS, LEGS;
    public static final ForgeConfigSpec.EnumValue<Appearance.Headwear> HEADWEAR;
    public static final ForgeConfigSpec.EnumValue<Appearance.Top> TOP;
    public static final ForgeConfigSpec.EnumValue<Appearance.Bottom> BOTTOM;
    public static final ForgeConfigSpec.EnumValue<Appearance.Footwear> FEET;
    private static final Map<BodyDimension, ForgeConfigSpec.DoubleValue> DIMENSIONS =
            new EnumMap<>(BodyDimension.class);

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
        b.push("dimensions");
        for (BodyDimension dimension : BodyDimension.values()) {
            DIMENSIONS.put(dimension, b.defineInRange(dimension.configName(),
                    dimension.standard(), dimension.min(), dimension.max()));
        }
        b.pop();
        b.pop();
        SPEC = b.build();
    }

    private ClientConfig() {}

    public static Appearance readAppearance() {
        return new Appearance(HEAD.get(), TORSO.get(), ARMS.get(), LEGS.get(),
                HEADWEAR.get(), TOP.get(), BOTTOM.get(), FEET.get());
    }

    public static EnumMap<BodyDimension, Double> readDimensions() {
        EnumMap<BodyDimension, Double> values = new EnumMap<>(BodyDimension.class);
        for (BodyDimension dimension : BodyDimension.values()) {
            values.put(dimension, DIMENSIONS.get(dimension).get());
        }
        return values;
    }

    public static void save(Appearance appearance, Map<BodyDimension, Double> dimensions) {
        HEAD.set(appearance.head());
        TORSO.set(appearance.torso());
        ARMS.set(appearance.arms());
        LEGS.set(appearance.legs());
        HEADWEAR.set(appearance.headwear());
        TOP.set(appearance.top());
        BOTTOM.set(appearance.bottom());
        FEET.set(appearance.feet());
        for (BodyDimension dimension : BodyDimension.values()) {
            DIMENSIONS.get(dimension).set(dimension.clamp(dimensions.get(dimension)));
        }
    }
}
