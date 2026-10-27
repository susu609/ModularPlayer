package com.susu609.modularplayer.appearance;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

/**
 * Editable control cage for the two generated surface meshes.
 *
 * Controls move an interpolated patch, not baked cubes. Offsets are in model
 * pixels, and deformation fades to zero at each patch boundary so it joins
 * the vanilla torso cleanly. Data is independent of Minecraft client classes.
 */
public final class MeshRig {
    public static final float LIMIT = 2.5F;

    public enum Part { CHEST, BUTTOCKS }
    public enum Side { LEFT, RIGHT }
    public enum Axis { X, Y, Z }

    public enum Handle {
        CENTER("Center", 0F, 0F),
        UPPER("Upper", 0F, -.65F),
        LOWER("Lower", 0F, .65F),
        INNER("Inner", .65F, 0F),
        OUTER("Outer", -.65F, 0F);

        private final String label;
        private final float u;
        private final float v;

        Handle(String label, float u, float v) {
            this.label = label;
            this.u = u;
            this.v = v;
        }

        public String label() { return label; }
        public float u(Side side) { return side == Side.LEFT ? u : -u; }
        public float v() { return v; }
    }

    /** Safe immutable offset vector. */
    public record Offset(float x, float y, float z) {
        public static final Offset ZERO = new Offset(0F, 0F, 0F);
        public Offset add(Axis axis, float delta) {
            return switch (axis) {
                case X -> new Offset(clamp(x + delta), y, z);
                case Y -> new Offset(x, clamp(y + delta), z);
                case Z -> new Offset(x, y, clamp(z + delta));
            };
        }
        public float at(Axis axis) {
            return switch (axis) { case X -> x; case Y -> y; case Z -> z; };
        }
        public Offset with(Axis axis, float next) {
            return add(axis, next - at(axis));
        }
    }

    public record Key(Part part, Side side, Handle handle, Axis axis) {
        public String configName() {
            return (part.name() + "_" + side.name() + "_" + handle.name() + "_" + axis.name())
                    .toLowerCase(Locale.ROOT);
        }
    }

    private final EnumMap<Part, EnumMap<Side, EnumMap<Handle, Offset>>> data =
            new EnumMap<>(Part.class);

    public MeshRig() {
        for (Part part : Part.values()) {
            EnumMap<Side, EnumMap<Handle, Offset>> sides = new EnumMap<>(Side.class);
            for (Side side : Side.values()) {
                EnumMap<Handle, Offset> points = new EnumMap<>(Handle.class);
                for (Handle handle : Handle.values()) points.put(handle, Offset.ZERO);
                sides.put(side, points);
            }
            data.put(part, sides);
        }
    }

    public Offset get(Part part, Side side, Handle handle) {
        return data.get(part).get(side).get(handle);
    }

    public float get(Key key) {
        return get(key.part(), key.side(), key.handle()).at(key.axis());
    }

    public void set(Key key, float value) {
        set(key.part(), key.side(), key.handle(), key.axis(), value);
    }

    public void set(Part part, Side side, Handle handle, Axis axis, float value) {
        EnumMap<Handle, Offset> points = data.get(part).get(side);
        points.put(handle, points.get(handle).with(axis, clamp(value)));
    }

    public void reset(Part part) {
        for (Side side : Side.values()) {
            for (Handle handle : Handle.values()) {
                data.get(part).get(side).put(handle, Offset.ZERO);
            }
        }
    }

    public MeshRig copy() {
        MeshRig clone = new MeshRig();
        for (Key key : keys()) clone.set(key, get(key));
        return clone;
    }

    public static List<Key> keys() {
        List<Key> keys = new ArrayList<>(Part.values().length * Side.values().length
                * Handle.values().length * Axis.values().length);
        for (Part part : Part.values())
            for (Side side : Side.values())
                for (Handle handle : Handle.values())
                    for (Axis axis : Axis.values())
                        keys.add(new Key(part, side, handle, axis));
        return keys;
    }

    /**
     * Weighted 2D radial basis. The rim mask pins the boundary to its parent
     * body mesh and avoids open seams when control points move.
     */
    public Offset sample(Part part, Side side, float u, float v) {
        float radiusSquared = u * u + v * v;
        if (radiusSquared >= 1F) return Offset.ZERO;
        float dx = 0F, dy = 0F, dz = 0F, weightSum = 0F;
        for (Handle handle : Handle.values()) {
            float du = u - handle.u(side);
            float dv = v - handle.v();
            float weight = (float) Math.exp(-5F * (du * du + dv * dv));
            Offset offset = get(part, side, handle);
            dx += offset.x() * weight;
            dy += offset.y() * weight;
            dz += offset.z() * weight;
            weightSum += weight;
        }
        float rim = (1F - radiusSquared);
        rim = rim * rim;
        if (weightSum < .0001F) return Offset.ZERO;
        return new Offset(dx / weightSum * rim, dy / weightSum * rim, dz / weightSum * rim);
    }

    private static float clamp(float value) {
        if (!Float.isFinite(value)) return 0F;
        return Math.max(-LIMIT, Math.min(LIMIT, value));
    }
}
