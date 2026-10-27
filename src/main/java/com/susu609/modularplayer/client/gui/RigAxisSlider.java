package com.susu609.modularplayer.client.gui;

import com.susu609.modularplayer.appearance.MeshRig;
import com.susu609.modularplayer.client.AppearanceState;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Edits one axis of a selected weighted cage handle; no disk writes here. */
public final class RigAxisSlider extends AbstractSliderButton {
    public enum SideMode {
        BOTH, LEFT, RIGHT;
        public SideMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final MeshRig.Part part;
    private final MeshRig.Handle handle;
    private final MeshRig.Axis axis;
    private final SideMode mode;

    public RigAxisSlider(int x, int y, int width,
                         MeshRig.Part part, MeshRig.Handle handle,
                         MeshRig.Axis axis, SideMode mode) {
        super(x, y, width, 20, Component.empty(),
                (read(part, handle, axis, mode) + MeshRig.LIMIT)
                        / (MeshRig.LIMIT * 2F));
        this.part = part;
        this.handle = handle;
        this.axis = axis;
        this.mode = mode;
        updateMessage();
    }

    public static float read(MeshRig.Part part, MeshRig.Handle handle,
                             MeshRig.Axis axis, SideMode mode) {
        MeshRig.Side side = mode == SideMode.RIGHT
                ? MeshRig.Side.RIGHT : MeshRig.Side.LEFT;
        return AppearanceState.rig().get(part, side, handle).at(axis);
    }

    public static void write(MeshRig.Part part, MeshRig.Handle handle,
                             MeshRig.Axis axis, SideMode mode, float value) {
        MeshRig rig = AppearanceState.rig();
        if (mode == SideMode.BOTH) {
            rig.set(part, MeshRig.Side.LEFT, handle, axis, value);
            rig.set(part, MeshRig.Side.RIGHT, handle, axis,
                    axis == MeshRig.Axis.X ? -value : value);
        } else {
            rig.set(part, mode == SideMode.LEFT ? MeshRig.Side.LEFT : MeshRig.Side.RIGHT,
                    handle, axis, value);
        }
    }

    @Override
    protected void applyValue() {
        write(part, handle, axis, mode,
                (float) (value * MeshRig.LIMIT * 2F - MeshRig.LIMIT));
    }

    @Override
    protected void updateMessage() {
        float actual = (float) (value * MeshRig.LIMIT * 2F - MeshRig.LIMIT);
        setMessage(Component.literal("Offset " + axis.name() + ": "
                + String.format(java.util.Locale.ROOT, "%+.2f px", actual)));
    }
}
