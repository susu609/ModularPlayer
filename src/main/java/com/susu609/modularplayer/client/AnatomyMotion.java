package com.susu609.modularplayer.client;

import com.susu609.modularplayer.appearance.BodyDimension;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/**
 * Bounded, discrete spring-damper secondary motion, once per client tick.
 * Uses local-space acceleration, turn rate, vertical landings and emote force.
 * Chest and glutes have independent stiffness, drag, mass-like response and
 * left/right channels; appearance-only, no collisions or rigid-body physics.
 */
public final class AnatomyMotion {
    private static final int CHEST = 0;
    private static final int BUTTOCKS = 1;
    private static final float[][] vertical = new float[2][2];
    private static final float[][] lateral = new float[2][2];
    private static final float[][] speedY = new float[2][2];
    private static final float[][] speedX = new float[2][2];
    private static UUID playerId;
    private static Vec3 previousVelocity = Vec3.ZERO;
    private static float previousYaw;
    private static boolean previousGround;
    private static boolean seeded;
    private static float prevSide, oldSide, prevLift, oldLift, prevTwist, oldTwist;

    private AnatomyMotion() {}

    public static void tick(LocalPlayer player) {
        if (player == null) {
            reset();
            return;
        }
        if (!player.getUUID().equals(playerId)) {
            reset();
            playerId = player.getUUID();
        }

        Vec3 velocity = player.getDeltaMovement();
        float yaw = player.getYRot();
        boolean grounded = player.onGround();
        if (!seeded) {
            previousVelocity = velocity;
            previousYaw = yaw;
            previousGround = grounded;
            seeded = true;
        }

        Vec3 acceleration = velocity.subtract(previousVelocity);
        float yawDelta = net.minecraft.util.Mth.wrapDegrees(yaw - previousYaw);
        double angle = Math.toRadians(yaw);
        float horizontal = (float) (acceleration.x * Math.cos(angle)
                                  + acceleration.z * Math.sin(angle));
        float sideways = (float) (-acceleration.x * Math.sin(angle)
                                + acceleration.z * Math.cos(angle));
        float verticalAcceleration = (float) acceleration.y;
        float landing = grounded && !previousGround
                ? Math.min(1F, (float) Math.abs(previousVelocity.y) * 2F) : 0F;
        // Second derivative of the animated torso pose (discrete acceleration).
        float side = EmoteState.torsoSway();
        float lift = EmoteState.torsoLift();
        float twist = EmoteState.torsoTwist();
        float sideAccel = side - 2F * prevSide + oldSide;
        float liftAccel = lift - 2F * prevLift + oldLift;
        float twistAccel = twist - 2F * prevTwist + oldTwist;
        oldSide = prevSide; prevSide = side;
        oldLift = prevLift; prevLift = lift;
        oldTwist = prevTwist; prevTwist = twist;

        simulate(CHEST,
                (float) AppearanceState.get(BodyDimension.CHEST_SWAY),
                (float) AppearanceState.get(BodyDimension.CHEST_FIRMNESS),
                (float) AppearanceState.get(BodyDimension.CHEST_DAMPING),
                horizontal, sideways, verticalAcceleration, yawDelta, landing,
                sideAccel, liftAccel, twistAccel);
        simulate(BUTTOCKS,
                (float) AppearanceState.get(BodyDimension.BUTTOCK_SWAY),
                (float) AppearanceState.get(BodyDimension.BUTTOCK_FIRMNESS),
                (float) AppearanceState.get(BodyDimension.BUTTOCK_DAMPING),
                horizontal, sideways, verticalAcceleration, yawDelta, landing,
                sideAccel, liftAccel, twistAccel);

        previousVelocity = velocity;
        previousYaw = yaw;
        previousGround = grounded;
    }

    private static void simulate(int part, float amount, float stiffness, float damping,
                                 float forward, float strafe, float up, float turn,
                                 float landing, float sideAccel, float liftAccel, float twistAccel) {
        if (amount < .0001F) {
            zero(part);
            return;
        }
        // Stable per-tick solver. Higher stiffness responds faster; higher
        // damping reduces residual oscillation, not the initial impulse.
        float spring = .09F + .25F * stiffness;
        float drag = .90F - .45F * damping;
        float sensitivity = part == CHEST ? 1F : .75F;
        for (int side = 0; side < 2; side++) {
            float direction = side == 0 ? -1F : 1F;
            float targetY = amount * sensitivity * (
                    -up * 2.8F + landing * .35F
                    + liftAccel * 12F * (part == CHEST ? 1F : .8F));
            float targetX = amount * sensitivity * (
                    -strafe * 2.4F - turn * .013F - forward * .45F
                    + sideAccel * 7F + twistAccel * 6F * direction);
            // Tiny left/right difference rather than completely robotic sync.
            float asymmetry = side == 0 ? 1F : .97F;
            targetY = clamp(targetY * asymmetry, -.72F, .72F);
            targetX = clamp(targetX * asymmetry, -.52F, .52F);
            speedY[part][side] = (speedY[part][side]
                    + (targetY - vertical[part][side]) * spring) * drag;
            speedX[part][side] = (speedX[part][side]
                    + (targetX - lateral[part][side]) * spring) * drag;
            vertical[part][side] = clamp(vertical[part][side]
                    + speedY[part][side], -.75F, .75F);
            lateral[part][side] = clamp(lateral[part][side]
                    + speedX[part][side], -.55F, .55F);
        }
    }

    /** rear=false: chest; rear=true: glutes. side=-1: left; +1: right. */
    public static float verticalPixels(boolean rear, int side) {
        return vertical[rear ? BUTTOCKS : CHEST][side < 0 ? 0 : 1];
    }

    public static float lateralPixels(boolean rear, int side) {
        return lateral[rear ? BUTTOCKS : CHEST][side < 0 ? 0 : 1];
    }

    private static float clamp(float value, float low, float high) {
        return Math.max(low, Math.min(high, value));
    }

    private static void zero(int part) {
        for (int side = 0; side < 2; side++) {
            vertical[part][side] = lateral[part][side] = 0F;
            speedY[part][side] = speedX[part][side] = 0F;
        }
    }

    public static void reset() {
        zero(CHEST);
        zero(BUTTOCKS);
        playerId = null;
        previousVelocity = Vec3.ZERO;
        previousYaw = 0F;
        previousGround = false;
        seeded = false;
        prevSide = oldSide = prevLift = oldLift = prevTwist = oldTwist = 0F;
    }
}
