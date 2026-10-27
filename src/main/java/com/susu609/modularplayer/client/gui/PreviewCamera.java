package com.susu609.modularplayer.client.gui;

import com.mojang.math.Axis;
import org.joml.Quaternionf;

/**
 * Preview camera presets. Isometric views use a 45 degree yaw and the
 * standard 35.264 degree elevation (atan(1/sqrt(2))).
 *
 * Only the preview quaternion is changed. Player rotation, the world camera,
 * body proportions and saved avatar appearance are never modified.
 */
public final class PreviewCamera {
    public static final float ISOMETRIC_ELEVATION = 35.2643897F;

    public enum Preset {
        ISOMETRIC_LEFT("ISO Left", -45F, ISOMETRIC_ELEVATION),
        ISOMETRIC_RIGHT("ISO Right", 45F, ISOMETRIC_ELEVATION),
        FRONT("Front", 0F, 10F),
        SIDE("Side", 90F, 10F),
        BACK("Back", 180F, 10F),
        FREE("Free", 0F, 0F);

        private final String label;
        private final float yaw;
        private final float pitch;

        Preset(String label, float yaw, float pitch) {
            this.label = label;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public String label() { return label; }
        public float yaw() { return yaw; }
        public float pitch() { return pitch; }
    }

    private Preset preset = Preset.ISOMETRIC_LEFT;
    private float freeYaw = preset.yaw();
    private float freePitch = preset.pitch();

    public Preset preset() { return preset; }
    public String label() { return preset.label(); }

    public float yaw() {
        return preset == Preset.FREE ? freeYaw : preset.yaw();
    }

    public float pitch() {
        return preset == Preset.FREE ? freePitch : preset.pitch();
    }

    /** Cycle fixed views; a drag changes to FREE without losing its start pose. */
    public void nextPreset() {
        preset = switch (preset) {
            case ISOMETRIC_LEFT -> Preset.ISOMETRIC_RIGHT;
            case ISOMETRIC_RIGHT -> Preset.FRONT;
            case FRONT -> Preset.SIDE;
            case SIDE -> Preset.BACK;
            case BACK, FREE -> Preset.ISOMETRIC_LEFT;
        };
        freeYaw = preset.yaw();
        freePitch = preset.pitch();
    }

    public void resetIsometric() {
        preset = Preset.ISOMETRIC_LEFT;
        freeYaw = preset.yaw();
        freePitch = preset.pitch();
    }

    public void drag(double horizontal, double vertical) {
        if (preset != Preset.FREE) {
            freeYaw = preset.yaw();
            freePitch = preset.pitch();
            preset = Preset.FREE;
        }
        freeYaw = (freeYaw + (float) horizontal * 1.15F) % 360F;
        freePitch = Math.max(-75F, Math.min(75F, freePitch + (float) vertical * .6F));
    }

    /** Preserve the existing Minecraft GUI 180-degree Z orientation. */
    public Quaternionf quaternion() {
        return Axis.ZP.rotationDegrees(180F)
                .mul(Axis.XP.rotationDegrees(pitch()))
                .mul(Axis.YP.rotationDegrees(yaw()));
    }
}
