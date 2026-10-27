package com.susu609.modularplayer.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.susu609.modularplayer.appearance.Appearance;
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

/** Vanilla animation source + replaceable body meshes + independent outfit textures. */
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
        body(a.head(), "head", head, pose, vertices, light, overlay, red, green, blue, alpha);
        body(a.torso(), "body", body, pose, vertices, light, overlay, red, green, blue, alpha);
        body(a.arms(), "right_arm", rightArm, pose, vertices, light, overlay, red, green, blue, alpha);
        body(a.arms(), "left_arm", leftArm, pose, vertices, light, overlay, red, green, blue, alpha);
        body(a.legs(), "right_leg", rightLeg, pose, vertices, light, overlay, red, green, blue, alpha);
        body(a.legs(), "left_leg", leftLeg, pose, vertices, light, overlay, red, green, blue, alpha);
    }

    private void body(Appearance.Shape shape, String part, ModelPart poseSource, PoseStack pose,
                      VertexConsumer vertices, int light, int overlay,
                      float red, float green, float blue, float alpha) {
        ModelPart target = part(shape, part, poseSource);
        target.render(pose, vertices, light, overlay, red, green, blue, alpha);
    }

    private ModelPart part(Appearance.Shape shape, String name, ModelPart source) {
        ModelPart part = shapes.getChild(shape == Appearance.Shape.WIDE ? "wide" : "steve").getChild(name);
        part.copyFrom(source);
        return part;
    }

    public void renderClothes(PoseStack pose, MultiBufferSource buffers, int light,
                              AbstractClientPlayer player) {
        if (!active || player.isInvisible()) return;
        Appearance a = AppearanceState.current();
        if (a.headwear() == Appearance.Headwear.STEVE_HAT
                && player.isModelPartShown(PlayerModelPart.HAT)) {
            outfit(a.head(), "hat", head, pose, buffers, light, "outfit/steve_hat.png");
        }
        if (a.top() != Appearance.Top.NONE) {
            String texture = a.top() == Appearance.Top.STEVE_RED
                    ? "outfit/steve_shirt_red.png" : "outfit/steve_shirt_blue.png";
            if (player.isModelPartShown(PlayerModelPart.JACKET)) {
                outfit(a.torso(), "jacket", body, pose, buffers, light, texture);
            }
            if (player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE)) {
                outfit(a.arms(), "right_sleeve", rightArm, pose, buffers, light, texture);
            }
            if (player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE)) {
                outfit(a.arms(), "left_sleeve", leftArm, pose, buffers, light, texture);
            }
        }
        if (a.bottom() == Appearance.Bottom.STEVE_PANTS) {
            if (player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG)) {
                outfit(a.legs(), "right_pants", rightLeg, pose, buffers, light, "outfit/steve_pants.png");
            }
            if (player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG)) {
                outfit(a.legs(), "left_pants", leftLeg, pose, buffers, light, "outfit/steve_pants.png");
            }
        }
        if (a.feet() == Appearance.Footwear.STEVE_BOOTS) {
            outfit(a.legs(), "right_boot", rightLeg, pose, buffers, light, "outfit/steve_boots.png");
            outfit(a.legs(), "left_boot", leftLeg, pose, buffers, light, "outfit/steve_boots.png");
        }
    }

    private void outfit(Appearance.Shape shape, String name, ModelPart source, PoseStack pose,
                        MultiBufferSource buffers, int light, String texture) {
        ResourceLocation location = new ResourceLocation(ID, "textures/" + texture);
        ModelPart mesh = part(shape, name, source);
        mesh.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(location)),
                light, OverlayTexture.NO_OVERLAY);
    }
}
