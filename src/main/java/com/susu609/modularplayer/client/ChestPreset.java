package com.susu609.modularplayer.client;

import com.susu609.modularplayer.appearance.BodyDimension;
import com.susu609.modularplayer.appearance.BreastShapeProfile;

/**
 * Reproducible editor presets. Only CHEST fields are changed; head, limbs,
 * outfits and all buttock/hip settings are preserved.
 */
public enum ChestPreset {
    JENNY_INSPIRED("Jenny-inspired"),
    NATURAL("Natural"),
    ROUND("Round"),
    SOFT("Soft motion"),
    STEVE("Reset to Steve");

    private final String label;
    ChestPreset(String label) { this.label = label; }
    public String label() { return label; }

    public void apply() {
        for (BodyDimension d : BodyDimension.values()) {
            if (d.group() == BodyDimension.Group.CHEST) {
                AppearanceState.set(d, d.standard());
            }
        }
        switch (this) {
            case STEVE -> { /* Default flat shape, zero spring motion. */ }
            case JENNY_INSPIRED -> {
                AppearanceState.set(BodyDimension.CHEST_STYLE, BreastShapeProfile.JENNY_INSPIRED.ordinal());
                AppearanceState.set(BodyDimension.CHEST_VOLUME, 1.15);
                AppearanceState.set(BodyDimension.CHEST_FULLNESS, .68);
                AppearanceState.set(BodyDimension.CHEST_LIFT, .18);
                AppearanceState.set(BodyDimension.CHEST_SEPARATION, .38);
                AppearanceState.set(BodyDimension.CHEST_SWAY, .13);
            }
            case NATURAL -> {
                AppearanceState.set(BodyDimension.CHEST_STYLE, BreastShapeProfile.NATURAL.ordinal());
                AppearanceState.set(BodyDimension.CHEST_VOLUME, .85);
                AppearanceState.set(BodyDimension.CHEST_FULLNESS, .78);
                AppearanceState.set(BodyDimension.CHEST_LIFT, -.06);
                AppearanceState.set(BodyDimension.CHEST_SEPARATION, .42);
                AppearanceState.set(BodyDimension.CHEST_SWAY, .17);
                AppearanceState.set(BodyDimension.CHEST_DAMPING, .84);
            }
            case ROUND -> {
                AppearanceState.set(BodyDimension.CHEST_STYLE, BreastShapeProfile.ROUND.ordinal());
                AppearanceState.set(BodyDimension.CHEST_VOLUME, 1.05);
                AppearanceState.set(BodyDimension.CHEST_FULLNESS, .55);
                AppearanceState.set(BodyDimension.CHEST_LIFT, .08);
                AppearanceState.set(BodyDimension.CHEST_SWAY, .04);
            }
            case SOFT -> {
                AppearanceState.set(BodyDimension.CHEST_STYLE, BreastShapeProfile.NATURAL.ordinal());
                AppearanceState.set(BodyDimension.CHEST_VOLUME, 1.12);
                AppearanceState.set(BodyDimension.CHEST_FULLNESS, .85);
                AppearanceState.set(BodyDimension.CHEST_LIFT, -.17);
                AppearanceState.set(BodyDimension.CHEST_SEPARATION, .5);
                AppearanceState.set(BodyDimension.CHEST_SWAY, .52);
                AppearanceState.set(BodyDimension.CHEST_FIRMNESS, .33);
                AppearanceState.set(BodyDimension.CHEST_DAMPING, .62);
            }
        }
        AnatomyMotion.reset();
    }
}
