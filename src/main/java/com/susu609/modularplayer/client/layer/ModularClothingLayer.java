package com.susu609.modularplayer.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.susu609.modularplayer.client.ClientTarget;
import com.susu609.modularplayer.client.model.ModularPlayerModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

/** Separate render pass with independent atlas per equipped garment. */
public final class ModularClothingLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public ModularClothingLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!ClientTarget.appliesTo(player)) return;
        if (getParentModel() instanceof ModularPlayerModel modular) {
            modular.renderClothes(pose, buffers, light, player);
        }
    }
}
