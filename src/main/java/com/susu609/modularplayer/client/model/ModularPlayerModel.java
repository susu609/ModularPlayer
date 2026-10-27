package com.susu609.modularplayer.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.susu609.modularplayer.appearance.Appearance;
import com.susu609.modularplayer.appearance.BodyDimension;
import com.susu609.modularplayer.client.AppearanceState;
import com.susu609.modularplayer.client.ClientTarget;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;

import static com.susu609.modularplayer.ModularPlayerMod.ID;
import static com.susu609.modularplayer.appearance.BodyDimension.*;

/**
 * Player mesh assembled per-body-group with live slider scales.
 * ModelPart pose is copied from vanilla animation, scaling about its bone origin.
 */
public final class ModularPlayerModel extends PlayerModel<AbstractClientPlayer> {
    private final ModelPart shapes;
    private boolean active;

    public ModularPlayerModel(ModelPart root, boolean slim) {
        super(root, slim);
        this.shapes = root.getChild("hpm_variants");
    }

    @Override
    public void setupAnim(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        active = ClientTarget.appliesTo(player);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        if (!active) {
            super.renderToBuffer(pose, vertices, light, overlay, red, green, blue, alpha);
            return;
        }
        Appearance a = AppearanceState.current();
        renderShape(a.head(), "head", head, HEAD_WIDTH, HEAD_HEIGHT, HEAD_DEPTH,
                pose, vertices, light, overlay, red, green, blue, alpha);
        renderShape(a.torso(), "body", body, TORSO_WIDTH, TORSO_HEIGHT, TORSO_DEPTH,
                pose, vertices, light, overlay, red, green, blue, alpha);
        renderShape(a.arms(), "right_arm", rightArm, ARM_WIDTH, ARM_LENGTH, ARM_DEPTH,
                pose, vertices, light, overlay, red, green, blue, alpha);
        renderShape(a.arms(), "left_arm", leftArm, ARM_WIDTH, ARM_LENGTH, ARM_DEPTH,
                pose, vertices, light, overlay, red, green, blue, alpha);
        renderShape(a.legs(), "right_leg", rightLeg, LEG_WIDTH, LEG_LENGTH, LEG_DEPTH,
                pose, vertices, light, overlay, red, green, blue, alpha);
        renderShape(a.legs(), "left_leg", leftLeg, LEG_WIDTH, LEG_LENGTH, LEG_DEPTH,
                pose, vertices, light, overlay, red, green, blue, alpha);

        // Hide exposed skin protrusions where clothing is fitted over the shapes.
        if (a.top() == Appearance.Top.NONE) {
            renderCurves(pose, vertices, light, overlay, false, red, green, blue, alpha);
        }
        if (a.bottom() == Appearance.Bottom.NONE) {
            renderCurves(pose, vertices, light, overlay, true, red, green, blue, alpha);
        }
    }

    private void renderShape(Appearance.Shape shape, String name, ModelPart source,
                             BodyDimension x, BodyDimension y, BodyDimension z, PoseStack pose,
                             VertexConsumer vertices, int light, int overlay,
                             float red, float green, float blue, float alpha) {
        float offsetX = 0;
        if (name.endsWith("_leg")) {
            float hipOffset = (float) ((AppearanceState.get(HIP_WIDTH) - 1.0) * 1.8 / 16.0);
            offsetX = name.startsWith("right") ? -hipOffset : hipOffset;
        }
        renderScaled(part(shape, name, source), source, pose,
                (float) AppearanceState.get(x), (float) AppearanceState.get(y),
                (float) AppearanceState.get(z), offsetX, 0F,
                vertices, light, overlay, red, green, blue, alpha);
    }

    private void renderCurves(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                              boolean glutes, float red, float green, float blue, float alpha) {
        float volume = (float) AppearanceState.get(glutes ? BUTTOCK_VOLUME : CHEST_VOLUME);
        if (volume <= 0.001F) return;
        Appearance.Shape shape = AppearanceState.current().torso();
        float width = (float) AppearanceState.get(glutes ? HIP_WIDTH : TORSO_WIDTH);
        float height = (float) AppearanceState.get(TORSO_HEIGHT);
        // Neutral volume is zero: the extra cubes are omitted, not flattened to
        // produce z-fighting against the base torso.
        for (String name : glutes
                ? new String[]{"glute_left", "glute_right"}
                : new String[]{"chest_left", "chest_right"}) {
            renderScaled(part(shape, name, body), body, pose,
                    width, height, Math.max(0.05F, volume), 0F,
                    curveOffset(glutes, volume), vertices, light, overlay, red, green, blue, alpha);
        }
    }

    private ModelPart part(Appearance.Shape shape, String name, ModelPart source) {
        ModelPart part = shapes.getChild(shape == Appearance.Shape.WIDE ? "wide" : "steve")
                .getChild(name);
        part.copyFrom(source);
        return part;
    }

    /** Scale a bone about its pivot, not about the global player origin. */
    private static void renderScaled(ModelPart target, ModelPart source, PoseStack pose,
                                     float x, float y, float z, float offsetX, float offsetZ,
                                     VertexConsumer vertices, int light, int overlay,
                                     float red, float green, float blue, float alpha) {
        pose.pushPose();
        pose.translate(offsetX, 0, offsetZ);
        pose.translate(source.x / 16F, source.y / 16F, source.z / 16F);
        pose.scale(x, y, z);
        pose.translate(-source.x / 16F, -source.y / 16F, -source.z / 16F);
        target.render(pose, vertices, light, overlay, red, green, blue, alpha);
        pose.popPose();
    }

    public void renderClothes(PoseStack pose, MultiBufferSource buffers, int light,
                              AbstractClientPlayer player) {
        if (!active || player.isInvisible()) return;
        Appearance a = AppearanceState.current();
        if (a.headwear() == Appearance.Headwear.STEVE_HAT
                && player.isModelPartShown(PlayerModelPart.HAT)) {
            outfit(a.head(), "hat", head, HEAD_WIDTH, HEAD_HEIGHT, HEAD_DEPTH,
                    pose, buffers, light, "outfit/steve_hat.png");
        }
        if (a.top() != Appearance.Top.NONE) {
            String texture = a.top() == Appearance.Top.STEVE_RED
                    ? "outfit/steve_shirt_red.png" : "outfit/steve_shirt_blue.png";
            if (player.isModelPartShown(PlayerModelPart.JACKET)) {
                outfit(a.torso(), "jacket", body, TORSO_WIDTH, TORSO_HEIGHT, TORSO_DEPTH,
                        pose, buffers, light, texture);
            }
            if (player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE)) {
                outfit(a.arms(), "right_sleeve", rightArm, ARM_WIDTH, ARM_LENGTH, ARM_DEPTH,
                        pose, buffers, light, texture);
            }
            if (player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE)) {
                outfit(a.arms(), "left_sleeve", leftArm, ARM_WIDTH, ARM_LENGTH, ARM_DEPTH,
                        pose, buffers, light, texture);
            }
            float breast = (float) AppearanceState.get(CHEST_VOLUME);
            if (breast > .001F && player.isModelPartShown(PlayerModelPart.JACKET)) {
                garmentCurve(a.torso(), false, breast, pose, buffers, light, texture);
            }
        }
        if (a.bottom() == Appearance.Bottom.STEVE_PANTS) {
            if (player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG)) {
                outfit(a.legs(), "right_pants", rightLeg, LEG_WIDTH, LEG_LENGTH, LEG_DEPTH,
                        pose, buffers, light, "outfit/steve_pants.png");
            }
            if (player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG)) {
                outfit(a.legs(), "left_pants", leftLeg, LEG_WIDTH, LEG_LENGTH, LEG_DEPTH,
                        pose, buffers, light, "outfit/steve_pants.png");
            }
            float butt = (float) AppearanceState.get(BUTTOCK_VOLUME);
            if (butt > .001F) garmentCurve(a.torso(), true, butt, pose, buffers, light,
                    "outfit/steve_pants.png");
        }
        if (a.feet() == Appearance.Footwear.STEVE_BOOTS) {
            outfit(a.legs(), "right_boot", rightLeg, LEG_WIDTH, LEG_LENGTH, LEG_DEPTH,
                    pose, buffers, light, "outfit/steve_boots.png");
            outfit(a.legs(), "left_boot", leftLeg, LEG_WIDTH, LEG_LENGTH, LEG_DEPTH,
                    pose, buffers, light, "outfit/steve_boots.png");
        }
    }

    private void outfit(Appearance.Shape shape, String name, ModelPart source,
                        BodyDimension x, BodyDimension y, BodyDimension z,
                        PoseStack pose, MultiBufferSource buffers, int light, String texture) {
        float hip = 0F;
        if (name.endsWith("_pants") || name.endsWith("_boot")) {
            float shift = (float)((AppearanceState.get(HIP_WIDTH) - 1) * 1.8 / 16);
            hip = name.startsWith("right") ? -shift : shift;
        }
        ResourceLocation location = new ResourceLocation(ID, "textures/" + texture);
        renderScaled(part(shape, name, source), source, pose,
                (float) AppearanceState.get(x), (float) AppearanceState.get(y),
                (float) AppearanceState.get(z), hip, 0F,
                buffers.getBuffer(RenderType.entityCutoutNoCull(location)),
                light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
    }

    /** Keep the front/back protrusion anchored at the adjusted torso surface. */
    private static float curveOffset(boolean glutes, float volume) {
        float torsoDepth = (float) AppearanceState.get(TORSO_DEPTH);
        return (glutes ? 1F : -1F) * (2F * torsoDepth - 2F * volume) / 16F;
    }

    private void garmentCurve(Appearance.Shape shape, boolean glutes, float volume,
                              PoseStack pose, MultiBufferSource buffers, int light, String texture) {
        ResourceLocation location = new ResourceLocation(ID, "textures/" + texture);
        float width = (float) AppearanceState.get(glutes ? HIP_WIDTH : TORSO_WIDTH);
        float height = (float) AppearanceState.get(TORSO_HEIGHT);
        for (String name : glutes
                ? new String[]{"glute_left", "glute_right"}
                : new String[]{"chest_left", "chest_right"}) {
            renderScaled(part(shape, name, body), body, pose, width, height,
                    Math.max(.05F, volume) * 1.04F, 0F,
                    curveOffset(glutes, volume * 1.04F), buffers.getBuffer(RenderType.entityCutoutNoCull(location)),
                    light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
        }
    }
}
