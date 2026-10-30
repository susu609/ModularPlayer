package com.susu609.modularplayer.client.model;

import com.susu609.modularplayer.ModularPlayerMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Two distinct small meshes in local breast-pivot coordinates.
 * Shape follows the Gender Mod 4x5x3 box approach, with original UVs/code.
 * Body attachment and spring transforms are applied by GenderLayer.
 */
public final class BreastModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(ModularPlayerMod.ID, "breasts"), "main");
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart leftJacket;
    private final ModelPart rightJacket;

    public BreastModel(ModelPart root) {
        left = root.getChild("left");
        right = root.getChild("right");
        leftJacket = root.getChild("left_jacket");
        rightJacket = root.getChild("right_jacket");
    }

    public ModelPart part(boolean leftSide, boolean jacket) {
        if (jacket) return leftSide ? leftJacket : rightJacket;
        return leftSide ? left : right;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Each breast is rendered at its own pivot by the layer, so the
        // geometry here must never contain the player-space separation.
        CubeListBuilder skin = CubeListBuilder.create().texOffs(16, 20)
                .addBox(-2F, -2F, -3F, 4F, 5F, 3F);
        CubeListBuilder wear = CubeListBuilder.create().texOffs(16, 32)
                .addBox(-2F, -2F, -3F, 4F, 5F, 3F, new CubeDeformation(.20F));
        root.addOrReplaceChild("left", skin, PartPose.ZERO);
        root.addOrReplaceChild("right", skin, PartPose.ZERO);
        root.addOrReplaceChild("left_jacket", wear, PartPose.ZERO);
        root.addOrReplaceChild("right_jacket", wear, PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
