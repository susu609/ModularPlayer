package com.susu609.modularplayer.client.gui;

import com.susu609.modularplayer.appearance.BodyDimension;
import com.susu609.modularplayer.client.AppearanceState;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Slider updates world + 3D preview immediately, saves only on user confirmation. */
public final class BodySlider extends AbstractSliderButton {
    private final BodyDimension dimension;

    public BodySlider(int x, int y, int width, BodyDimension dimension) {
        super(x, y, width, 20, Component.empty(),
                (AppearanceState.get(dimension) - dimension.min())
                        / (dimension.max() - dimension.min()));
        this.dimension = dimension;
        updateMessage();
    }

    @Override
    protected void applyValue() {
        AppearanceState.set(dimension, dimension.min()
                + value * (dimension.max() - dimension.min()));
    }

    @Override
    protected void updateMessage() {
        double actual = dimension.min() + value * (dimension.max() - dimension.min());
        setMessage(Component.literal(dimension.label() + ": " + dimension.format(actual)));
    }
}
