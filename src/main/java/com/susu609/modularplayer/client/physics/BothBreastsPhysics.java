package com.susu609.modularplayer.client.physics;

import com.susu609.modularplayer.config.GenderConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

/** Local-player motion is sampled once a client tick, never once a render frame. */
public final class BothBreastsPhysics {
    private final BreastPhysics left = new BreastPhysics();
    private final BreastPhysics right = new BreastPhysics();
    private Vec3 previousVelocity = Vec3.ZERO;
    private float previousBodyYaw;
    private boolean previousGround;
    private boolean seeded;

    public BreastPhysics left() { return left; }
    public BreastPhysics right() { return right; }

    public void tick(LocalPlayer player) {
        Vec3 velocity = player.getDeltaMovement();
        float bodyYaw = player.yBodyRot;
        boolean grounded = player.onGround();
        if (!seeded) {
            previousVelocity = velocity;
            previousBodyYaw = bodyYaw;
            previousGround = grounded;
            seeded = true;
        }
        Vec3 acceleration = velocity.subtract(previousVelocity);
        float yawDelta = Mth.wrapDegrees(bodyYaw - previousBodyYaw);
        float yawRad = bodyYaw * Mth.DEG_TO_RAD;
        float lateral = (float) (-acceleration.x * Math.cos(yawRad)
                + acceleration.z * Math.sin(yawRad));
        float vertical = (float) acceleration.y;
        float landed = grounded && !previousGround
                ? (float) Math.min(1D, Math.abs(previousVelocity.y) * 2D) : 0F;
        float walkBob = Mth.sin(player.walkAnimation.position() * .6662F)
                * player.walkAnimation.speed() * .12F;

        float strength = GenderConfig.PHYSICS.get() && GenderConfig.visible()
                ? GenderConfig.BOUNCE.get().floatValue() : 0F;
        // Armor suppresses secondary motion in this foundation version;
        // the next stage will render fitted armor geometry.
        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) strength *= .20F;
        float upForce = (-vertical * 2.2F + landed * .65F + walkBob) * strength;
        float sideways = (-lateral * 2.4F - yawDelta * .012F) * strength;
        float stiffness = GenderConfig.STIFFNESS.get().floatValue();
        float damping = GenderConfig.DAMPING.get().floatValue();
        left.tick(sideways, upForce, -yawDelta * .12F * strength, stiffness, damping);
        right.tick(sideways, upForce, yawDelta * .12F * strength, stiffness, damping);

        previousVelocity = velocity;
        previousBodyYaw = bodyYaw;
        previousGround = grounded;
    }
}
