package com.susu609.modularplayer.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.susu609.modularplayer.appearance.BodyDimension;
import com.susu609.modularplayer.client.AnatomyMotion;
import com.susu609.modularplayer.client.AppearanceState;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Original procedural low-poly convex surfaces for two separate torso lobes.
 *
 * Inspiration: layered, rotated, zero-thickness polygon panels observed in
 * Jenny's Bedrock geometry. None of its code, vertices, UV data or textures
 * are imported. These surfaces use our own rings, radius profile and vanilla
 * Steve's torso UV regions, with the same math for body and garments.
 *
 * Coordinates below are Minecraft model pixels relative to the torso pivot.
 */
public final class AnatomySurface {
    private static final int ANGLES = 16;
    private static final int RINGS = 5;
    private static final float TEXTURE_SIZE = 64F;
    private static final float CLOTH_GAP = 0.18F;

    private AnatomySurface() {}

    public static void render(PoseStack poses, VertexConsumer sink, ModelPart torso,
                              boolean rear, boolean dressed, int light, int overlay,
                              float red, float green, float blue, float alpha) {
        BodyDimension projectionDim = rear ? BodyDimension.BUTTOCK_VOLUME : BodyDimension.CHEST_VOLUME;
        float volume = (float) AppearanceState.get(projectionDim);
        if (volume <= 0.001F) return;

        float sizeX = (float) AppearanceState.get(rear ? BodyDimension.BUTTOCK_WIDTH
                : BodyDimension.CHEST_WIDTH);
        float sizeY = (float) AppearanceState.get(rear ? BodyDimension.BUTTOCK_HEIGHT
                : BodyDimension.CHEST_HEIGHT);
        float separation = (float) AppearanceState.get(rear ? BodyDimension.BUTTOCK_SEPARATION
                : BodyDimension.CHEST_SEPARATION);
        float vertical = (float) AppearanceState.get(rear ? BodyDimension.BUTTOCK_POSITION
                : BodyDimension.CHEST_POSITION);
        float overallWidth = (float) AppearanceState.get(rear ? BodyDimension.HIP_WIDTH
                : BodyDimension.TORSO_WIDTH);
        float overallHeight = (float) AppearanceState.get(BodyDimension.TORSO_HEIGHT);
        float overallDepth = (float) AppearanceState.get(BodyDimension.TORSO_DEPTH);

        // Heights are measured from the vanilla torso's upper pivot.
        float centerY = (rear ? 10F : 4.45F) * overallHeight - vertical;
        float ry = (rear ? 1.8F : 2.1F) * sizeY * overallHeight;
        float rx = 1.85F * sizeX * overallWidth;
        float depth = volume * 2.1F;
        float zBase = (rear ? 1F : -1F) * (2F * overallDepth + (dressed ? CLOTH_GAP : 0F));
        float outward = rear ? 1F : -1F;
        float uOffset = rear ? 32F : 20F; // torso back / front on Steve 64x64 atlas
        float verticalSway = rear ? 0F : AnatomyMotion.verticalPixels();
        float lateralSway = rear ? 0F : AnatomyMotion.lateralPixels();

        poses.pushPose();
        torso.translateAndRotate(poses);
        // PlayerModel cube coordinates use 1 model pixel = 1/16 world unit.
        poses.scale(1F / 16F, 1F / 16F, 1F / 16F);

        for (int side : new int[]{-1, 1}) {
            float cx = side * (2F * overallWidth + separation * .5F) + lateralSway * .35F;
            float cy = centerY - verticalSway * (side == 1 ? .95F : 1.05F);
            for (int ring = 0; ring < RINGS; ring++) {
                float inner = (float) ring / RINGS;
                float outer = (float) (ring + 1) / RINGS;
                for (int angle = 0; angle < ANGLES; angle++) {
                    double start = Math.PI * 2D * angle / ANGLES;
                    double end = Math.PI * 2D * (angle + 1) / ANGLES;
                    Point a = point(inner, start, cx, cy, rx, ry, zBase, outward, depth,
                            overallWidth, overallHeight, uOffset);
                    Point b = point(inner, end, cx, cy, rx, ry, zBase, outward, depth,
                            overallWidth, overallHeight, uOffset);
                    Point c = point(outer, end, cx, cy, rx, ry, zBase, outward, depth,
                            overallWidth, overallHeight, uOffset);
                    Point d = point(outer, start, cx, cy, rx, ry, zBase, outward, depth,
                            overallWidth, overallHeight, uOffset);
                    // Front faces -Z; rear faces +Z. Correct winding also
                    // keeps compatibility with culling render layers.
                    if (rear) {
                        vertex(sink, poses, a, light, overlay, red, green, blue, alpha);
                        vertex(sink, poses, d, light, overlay, red, green, blue, alpha);
                        vertex(sink, poses, c, light, overlay, red, green, blue, alpha);
                        vertex(sink, poses, b, light, overlay, red, green, blue, alpha);
                    } else {
                        vertex(sink, poses, a, light, overlay, red, green, blue, alpha);
                        vertex(sink, poses, b, light, overlay, red, green, blue, alpha);
                        vertex(sink, poses, c, light, overlay, red, green, blue, alpha);
                        vertex(sink, poses, d, light, overlay, red, green, blue, alpha);
                    }
                }
            }
        }
        poses.popPose();
    }

    private static Point point(float radius, double angle, float cx, float cy,
                               float rx, float ry, float base, float out,
                               float depth, float torsoWidth, float torsoHeight, float uOffset) {
        float u = radius * (float) Math.cos(angle);
        float v = radius * (float) Math.sin(angle);
        float x = cx + rx * u;
        float y = cy + ry * v;
        // Quartic falloff: smooth join at silhouette with the torso plane.
        float falloff = 1F - radius * radius;
        float z = base + out * depth * falloff * falloff;

        // The outward normal of z = base +/- depth*(1-r*r)^2.
        float nx = depth * 4F * u * falloff / Math.max(rx, .001F);
        float ny = depth * 4F * v * falloff / Math.max(ry, .001F);
        float nz = out;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

        float texU = (uOffset + 4F + x / Math.max(.01F, torsoWidth)) / TEXTURE_SIZE;
        float texV = (20F + y / Math.max(.01F, torsoHeight)) / TEXTURE_SIZE;
        return new Point(x, y, z, texU, texV, nx / length, ny / length, nz / length);
    }

    private static void vertex(VertexConsumer sink, PoseStack poses, Point point,
                               int light, int overlay, float r, float g, float b, float a) {
        sink.vertex(poses.last().pose(), point.x, point.y, point.z)
                .color(r, g, b, a)
                .uv(point.u, point.v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(poses.last().normal(), point.nx, point.ny, point.nz)
                .endVertex();
    }

    private record Point(float x, float y, float z, float u, float v,
                         float nx, float ny, float nz) {}
}
