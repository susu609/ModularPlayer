package com.susu609.modularplayer.client;

import com.susu609.modularplayer.appearance.BodyDimension;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/**
 * Two independent stable spring channels, ticked only on the client.
 * Shape, volume, and movement strength are independent knobs.
 * Disabled motion has exactly zero offsets and preserves Steve's idle pose.
 *
 * This is visual-only and has no effect on gameplay or entity dimensions.
 */
public final class AnatomyMotion {
    private static UUID currentPlayer;
    private static final float[] vertical = new float[2];
    private static final float[] horizontal = new float[2];
    private static final float[] velocityY = new float[2];
    private static final float[] velocityX = new float[2];

    private AnatomyMotion() {}

    public static void tick(LocalPlayer player) {
        if (player == null) {
            reset();
            return;
        }
        if (!player.getUUID().equals(currentPlayer)) {
            reset();
            currentPlayer = player.getUUID();
        }

        float strength = (float) AppearanceState.get(BodyDimension.CHEST_SWAY);
        if (strength <= .0001F) {
            zeroMotion();
            return;
        }

        // Independent stiffness/drag controls inspired by SpringBone systems,
        // not third-party physical simulation code.
        float firmness = (float) AppearanceState.get(BodyDimension.CHEST_FIRMNESS);
        float damping = (float) AppearanceState.get(BodyDimension.CHEST_DAMPING);
        float spring = .08F + .34F * firmness;
        float drag = .52F + .43F * damping;
        Vec3 motion = player.getDeltaMovement();
        double yaw = Math.toRadians(player.getYRot());
        float sideways = (float) (motion.x * Math.cos(yaw) + motion.z * Math.sin(yaw));

        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? -1F : 1F;
            // Slightly different phase/response prevents perfectly robotic
            // mirroring while both channels share user-facing settings.
            float response = i == 0 ? 1F : .96F;
            float targetY = clamp((float)(-motion.y * 2.1F) * strength * response,
                    -.65F, .65F);
            float targetX = clamp(-sideways * 1.65F * strength
                    + side * Math.abs(sideways) * .13F * strength, -.42F, .42F);
            velocityY[i] = (velocityY[i] + (targetY - vertical[i]) * spring) * drag;
            velocityX[i] = (velocityX[i] + (targetX - horizontal[i]) * spring) * drag;
            vertical[i] = clamp(vertical[i] + velocityY[i], -.70F, .70F);
            horizontal[i] = clamp(horizontal[i] + velocityX[i], -.45F, .45F);
        }
    }

    /** @param side -1: left; +1: right. */
    public static float verticalPixels(int side) {
        return vertical[side < 0 ? 0 : 1];
    }

    /** @param side -1: left; +1: right. */
    public static float lateralPixels(int side) {
        return horizontal[side < 0 ? 0 : 1];
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static void zeroMotion() {
        for (int i = 0; i < 2; i++) {
            vertical[i] = horizontal[i] = velocityY[i] = velocityX[i] = 0F;
        }
    }

    public static void reset() {
        zeroMotion();
        currentPlayer = null;
    }
}
