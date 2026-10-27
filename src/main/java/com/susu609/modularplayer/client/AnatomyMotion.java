package com.susu609.modularplayer.client;

import com.susu609.modularplayer.appearance.BodyDimension;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/**
 * Small, stable spring-damper simulation, independent of the player hitbox.
 * Runs once per client tick, not once per render pass (world + editor preview).
 * No third-party code or assets are used.
 */
public final class AnatomyMotion {
    private static UUID currentPlayer;
    private static float vertical;
    private static float lateral;
    private static float verticalVelocity;
    private static float lateralVelocity;

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
            vertical = 0F;
            lateral = 0F;
            verticalVelocity = 0F;
            lateralVelocity = 0F;
            return;
        }
        Vec3 velocity = player.getDeltaMovement();
        double yaw = Math.toRadians(player.getYRot());
        float sideways = (float) (velocity.x * Math.cos(yaw) + velocity.z * Math.sin(yaw));
        float targetY = clamp((float) (-velocity.y * 2.0F) * strength, -.55F, .55F);
        float targetX = clamp(-sideways * 1.7F * strength, -.35F, .35F);

        // Discrete, critically-damped approximation. All units are model pixels.
        verticalVelocity = (verticalVelocity + (targetY - vertical) * .23F) * .72F;
        lateralVelocity = (lateralVelocity + (targetX - lateral) * .23F) * .72F;
        vertical = clamp(vertical + verticalVelocity, -.65F, .65F);
        lateral = clamp(lateral + lateralVelocity, -.40F, .40F);
    }

    public static float verticalPixels() { return vertical; }
    public static float lateralPixels() { return lateral; }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void reset() {
        vertical = lateral = verticalVelocity = lateralVelocity = 0F;
        currentPlayer = null;
    }
}
