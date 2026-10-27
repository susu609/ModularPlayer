package com.susu609.modularplayer.client;

import com.susu609.modularplayer.ClientConfig;
import com.susu609.modularplayer.appearance.Appearance;

/** Session-local editor state; defaults are read from client config on first access. */
public final class AppearanceState {
    private static Appearance current;

    private AppearanceState() {}

    public static Appearance current() {
        if (current == null) current = ClientConfig.readAppearance();
        return current;
    }

    public static void cycleTorso() {
        Appearance a = current();
        current = a.withTorso(next(a.torso(), Appearance.Shape.values()));
    }

    public static void cycleTop() {
        Appearance a = current();
        current = a.withTop(next(a.top(), Appearance.Top.values()));
    }

    public static void cycleBottom() {
        Appearance a = current();
        current = a.withBottom(next(a.bottom(), Appearance.Bottom.values()));
    }

    public static void cycleFeet() {
        Appearance a = current();
        current = a.withFeet(next(a.feet(), Appearance.Footwear.values()));
    }

    public static void reset() { current = ClientConfig.readAppearance(); }

    private static <E extends Enum<E>> E next(E value, E[] values) {
        return values[(value.ordinal() + 1) % values.length];
    }
}
