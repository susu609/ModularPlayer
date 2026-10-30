package com.susu609.modularplayer.client.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;

/**
 * Geometry baseline for modular avatar. Every variant has the same named bones.
 * Front is negative Z. Chest and glutes are separate meshes, not part of the Steve skin.
 */
public final class BodyGeometry {
    private BodyGeometry() {}

    public static LayerDefinition create(boolean slim) {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, slim);
        PartDefinition variants = mesh.getRoot().addOrReplaceChild("hpm_variants",
                CubeListBuilder.create(), PartPose.ZERO);
        addShape(variants, "steve", slim, false);
        addShape(variants, "wide", slim, true);
        addBreastRig(mesh.getRoot());
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * Four Blockbench reference cubes, rebased to independent left/right pivots.
     * Parent torso transform is applied during rendering; cubes are in body-local pixels.
     * The anchor is a transform node, not a mesh, so size sliders scale the flesh only.
     */
    private static void addBreastRig(PartDefinition root) {
        PartDefinition rig = root.addOrReplaceChild("mp_breasts",
                CubeListBuilder.create(), PartPose.ZERO);
        for (int side : new int[]{-1, 1}) {
            String name = side < 0 ? "left" : "right";
            float x = side * 2F;
            PartDefinition anchor = rig.addOrReplaceChild(name + "_anchor",
                    CubeListBuilder.create(), PartPose.offset(x, 2F, -2F));
            anchor.addOrReplaceChild("base", CubeListBuilder.create().texOffs(16, 16)
                    .addBox(-2F, -2F, -1F, 4F, 4F, 2F), PartPose.ZERO);
            anchor.addOrReplaceChild("volume", CubeListBuilder.create().texOffs(16, 16)
                    .addBox(-3F, -1F, -6F, 6F, 6F, 6F), PartPose.ZERO);
        }
    }

    private static void addShape(PartDefinition parent, String id, boolean slim, boolean wide) {
        PartDefinition group = parent.addOrReplaceChild(id, CubeListBuilder.create(), PartPose.ZERO);
        float bodyWidth = wide ? 10.0F : 8.0F;
        float armWidth = slim ? 3.0F : (wide ? 4.5F : 4.0F);
        float legWidth = wide ? 4.5F : 4.0F;
        float headWidth = wide ? 9.0F : 8.0F;
        float bodyX = -bodyWidth / 2F;
        float headX = -headWidth / 2F;
        float legX = -legWidth / 2F;
        float rightArmX = -armWidth + 1.0F;
        float leftArmX = -1.0F;
        float armY = slim ? -2.5F : -2.0F;

        group.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(headX, -8, -4, headWidth, 8, 8), PartPose.ZERO);
        group.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(bodyX, 0, -2, bodyWidth, 12, 4), PartPose.ZERO);
        group.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
                .addBox(rightArmX, armY, -2, armWidth, 12, 4), PartPose.ZERO);
        group.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48)
                .addBox(leftArmX, armY, -2, armWidth, 12, 4), PartPose.ZERO);
        group.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(legX, 0, -2, legWidth, 12, 4), PartPose.ZERO);
        group.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48)
                .addBox(legX, 0, -2, legWidth, 12, 4), PartPose.ZERO);

        // Chest and glute surfaces are now tessellated at runtime by
        // AnatomySurface instead of baked rectangular ModelPart cubes.
        CubeDeformation gap = new CubeDeformation(0.26F);
        group.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0)
                .addBox(headX, -8, -4, headWidth, 8, 8, gap), PartPose.ZERO);
        group.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32)
                .addBox(bodyX, 0, -2, bodyWidth, 12, 4, gap), PartPose.ZERO);
        group.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32)
                .addBox(rightArmX, armY, -2, armWidth, 12, 4, gap), PartPose.ZERO);
        group.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48)
                .addBox(leftArmX, armY, -2, armWidth, 12, 4, gap), PartPose.ZERO);
        group.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32)
                .addBox(legX, 0, -2, legWidth, 12, 4, gap), PartPose.ZERO);
        group.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48)
                .addBox(legX, 0, -2, legWidth, 12, 4, gap), PartPose.ZERO);
        group.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(0, 32)
                .addBox(legX, 8, -2, legWidth, 4, 4, new CubeDeformation(0.40F)), PartPose.ZERO);
        group.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(0, 48)
                .addBox(legX, 8, -2, legWidth, 4, 4, new CubeDeformation(0.40F)), PartPose.ZERO);
    }
}
