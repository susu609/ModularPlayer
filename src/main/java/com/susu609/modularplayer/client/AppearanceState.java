package com.susu609.modularplayer.client;

import com.susu609.modularplayer.ClientConfig;
import com.susu609.modularplayer.appearance.Appearance;
import com.susu609.modularplayer.appearance.BodyDimension;
import java.util.EnumMap;

/** In-memory live preview and save/cancel transaction for the in-game editor. */
public final class AppearanceState {
    private static Appearance current;
    private static EnumMap<BodyDimension, Double> dimensions;
    private static Appearance beforeEdit;
    private static EnumMap<BodyDimension, Double> dimensionsBeforeEdit;

    private AppearanceState() {}

    public static Appearance current() {
        if (current == null) current = ClientConfig.readAppearance();
        return current;
    }

    public static double get(BodyDimension dimension) {
        if (dimensions == null) dimensions = ClientConfig.readDimensions();
        return dimensions.get(dimension);
    }

    public static void set(BodyDimension dimension, double value) {
        get(dimension);
        dimensions.put(dimension, dimension.clamp(value));
    }

    public static void setAppearance(Appearance next) {
        current = next;
    }

    public static void beginEdit() {
        beforeEdit = current();
        if (dimensions == null) dimensions = ClientConfig.readDimensions();
        dimensionsBeforeEdit = new EnumMap<>(dimensions);
    }

    public static void cancelEdit() {
        if (beforeEdit != null) {
            current = beforeEdit;
            dimensions = dimensionsBeforeEdit;
        }
        beforeEdit = null;
        dimensionsBeforeEdit = null;
    }

    public static void saveEdit() {
        if (dimensions == null) dimensions = ClientConfig.readDimensions();
        ClientConfig.save(current(), dimensions);
        beforeEdit = null;
        dimensionsBeforeEdit = null;
    }

    public static void defaults() {
        current = new Appearance(Appearance.Shape.STEVE, Appearance.Shape.STEVE,
                Appearance.Shape.STEVE, Appearance.Shape.STEVE,
                Appearance.Headwear.NONE, Appearance.Top.STEVE_BLUE,
                Appearance.Bottom.STEVE_PANTS, Appearance.Footwear.NONE);
        if (dimensions == null) dimensions = ClientConfig.readDimensions();
        for (BodyDimension d : BodyDimension.values()) dimensions.put(d, d.standard());
    }

    public static void cycleTorso() {
        Appearance a = current();
        setAppearance(a.withTorso(next(a.torso(), Appearance.Shape.values())));
    }

    public static void cycleTop() {
        Appearance a = current();
        setAppearance(a.withTop(next(a.top(), Appearance.Top.values())));
    }

    public static void cycleBottom() {
        Appearance a = current();
        setAppearance(a.withBottom(next(a.bottom(), Appearance.Bottom.values())));
    }

    public static void cycleFeet() {
        Appearance a = current();
        setAppearance(a.withFeet(next(a.feet(), Appearance.Footwear.values())));
    }

    public static <E extends Enum<E>> E next(E value, E[] values) {
        return values[(value.ordinal() + 1) % values.length];
    }
}
