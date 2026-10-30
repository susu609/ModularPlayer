package com.susu609.modularplayer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.susu609.modularplayer.client.model.BreastModel;
import com.susu609.modularplayer.client.physics.BothBreastsPhysics;
import com.susu609.modularplayer.client.physics.BreastPhysics;
import com.susu609.modularplayer.client.physics.BreastPhysicsManager;
import com.susu609.modularplayer.config.GenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;

/**
 * Gender-Mod-style additive render layer. Vanilla PlayerModel and skin stay intact.
 * No overwrite of PlayerRenderer, no custom skin atlas and no Blockbench dependency.
 */
public final class GenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final BreastModel breasts;

    public GenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                       ModelPart root) {
        super(parent);
        this.breasts = new BreastModel(root);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!GenderConfig.visible() || player.isInvisible() || player.isSpectator()) return;
        // User-data/network stage is not implemented yet: do not impose our
        // local gender choice on all other players in multiplayer.
        if (Minecraft.getInstance().player == null ||
                !Minecraft.getInstance().player.getUUID().equals(player.getUUID())) return;
        // Fitted armor shell is a later stage. Never draw breasts through chestplates.
        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) return;

        float size = GenderConfig.BUST_SIZE.get().floatValue();
        float scale = .45F + size * 1.15F;
        float sx = scale * GenderConfig.WIDTH.get().floatValue();
        float sy = scale * GenderConfig.HEIGHT.get().floatValue();
        float sz = scale * GenderConfig.DEPTH.get().floatValue();
        BothBreastsPhysics state = BreastPhysicsManager.get(player.getUUID());
        VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull(player.getSkinTextureLocation()));

        pose.pushPose();
        getParentModel().body.translateAndRotate(pose);
        renderSide(pose, skin, light, false, sx, sy, sz, partialTicks,
                state == null ? null : state.right(), false);
        renderSide(pose, skin, light, true, sx, sy, sz, partialTicks,
                state == null ? null : state.left(), false);
        if (player.isModelPartShown(PlayerModelPart.JACKET)) {
            renderSide(pose, skin, light, false, sx, sy, sz, partialTicks,
                    state == null ? null : state.right(), true);
            renderSide(pose, skin, light, true, sx, sy, sz, partialTicks,
                    state == null ? null : state.left(), true);
        }
        pose.popPose();
    }

    private void renderSide(PoseStack pose, VertexConsumer skin, int light, boolean left,
                            float sx, float sy, float sz, float partialTicks,
                            BreastPhysics physics, boolean jacket) {
        float side = left ? -1F : 1F;
        float horizontal = physics == null ? 0F : physics.x(partialTicks);
        float vertical = physics == null ? 0F : physics.y(partialTicks);
        float twist = physics == null ? 0F : physics.twist(partialTicks);

        pose.pushPose();
        pose.translate((side * (2F + GenderConfig.SEPARATION.get().floatValue() / 2F)
                + horizontal) / 16F,
                (3F - GenderConfig.VERTICAL_OFFSET.get().floatValue() + vertical) / 16F,
                -2F / 16F);
        // Pivot is the attachment point, independent from volume/depth scale.
        pose.mulPose(Axis.YP.rotationDegrees(twist));
        pose.mulPose(Axis.XP.rotationDegrees(-18F * GenderConfig.BUST_SIZE.get().floatValue()
                + vertical * 9F));
        pose.mulPose(Axis.ZP.rotationDegrees(horizontal * (left ? -6F : 6F)));
        pose.scale(sx, sy, sz);
        breasts.part(left, jacket).render(pose, skin, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
