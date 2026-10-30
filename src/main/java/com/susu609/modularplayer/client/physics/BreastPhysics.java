package com.susu609.modularplayer.client.physics;

import net.minecraft.util.Mth;

/** Independent, bounded, per-side spring for position X/Y and twist. */
public final class BreastPhysics {
    private float x, y, twist;
    private float previousX, previousY, previousTwist;
    private float velocityX, velocityY, velocityTwist;

    public void tick(float targetX, float targetY, float targetTwist,
                     float stiffness, float damping) {
        previousX = x;
        previousY = y;
        previousTwist = twist;
        float k = .08F + .27F * stiffness;
        float drag = .91F - .50F * damping;
        velocityX = (velocityX + (Mth.clamp(targetX, -.55F, .55F) - x) * k) * drag;
        velocityY = (velocityY + (Mth.clamp(targetY, -.75F, .75F) - y) * k) * drag;
        velocityTwist = (velocityTwist + (Mth.clamp(targetTwist, -12F, 12F) - twist) * k) * drag;
        x = Mth.clamp(x + velocityX, -.55F, .55F);
        y = Mth.clamp(y + velocityY, -.75F, .75F);
        twist = Mth.clamp(twist + velocityTwist, -12F, 12F);
    }

    public float x(float partialTicks) {
        return Mth.lerp(Mth.clamp(partialTicks, 0F, 1F), previousX, x);
    }
    public float y(float partialTicks) {
        return Mth.lerp(Mth.clamp(partialTicks, 0F, 1F), previousY, y);
    }
    public float twist(float partialTicks) {
        return Mth.lerp(Mth.clamp(partialTicks, 0F, 1F), previousTwist, twist);
    }
}
