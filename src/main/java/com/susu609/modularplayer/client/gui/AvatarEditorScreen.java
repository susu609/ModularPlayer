package com.susu609.modularplayer.client.gui;

import com.mojang.math.Axis;
import com.susu609.modularplayer.appearance.Appearance;
import com.susu609.modularplayer.appearance.BodyDimension;
import com.susu609.modularplayer.client.AppearanceState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;

import static com.susu609.modularplayer.appearance.BodyDimension.Group.*;

/**
 * Bedrock-inspired editor: categories at left, current player preview at center,
 * parameter sliders and clothes at right. In-game, no external website required.
 */
public final class AvatarEditorScreen extends Screen {
    private static final BodyDimension.Group[] TABS = { HEAD, TORSO, ARMS, LEGS, SHAPE, OUTFIT };
    private static final String[] TAB_NAMES = { "Head", "Torso", "Arms", "Legs", "Shape", "Clothes" };
    private BodyDimension.Group selected = SHAPE;
    private boolean previewDragging;
    private float rotation;
    private int zoom = 0;
    private int leftWidth;
    private int rightWidth;
    private int rightX;
    private int previewX;
    private int previewLeft;
    private int previewRight;

    public AvatarEditorScreen() {
        super(Component.literal("Modular Player - Character Editor"));
        AppearanceState.beginEdit();
    }

    @Override
    protected void init() {
        leftWidth = Math.max(106, Math.min(158, width / 4));
        rightWidth = Math.max(130, Math.min(202, width / 3));
        rightX = width - rightWidth - 6;
        previewLeft = leftWidth + 10;
        previewRight = rightX - 6;
        previewX = (previewLeft + previewRight) / 2;

        for (int i = 0; i < TABS.length; i++) {
            final BodyDimension.Group tab = TABS[i];
            String text = (selected == tab ? "> " : "") + TAB_NAMES[i];
            addRenderableWidget(Button.builder(Component.literal(text), button -> {
                selected = tab;
                rebuildWidgets();
            }).bounds(8, 40 + i * 23, leftWidth - 12, 20).build());
        }

        int controlX = rightX + 7;
        int controlWidth = rightWidth - 14;
        int y = 54;
        if (selected == OUTFIT) {
            addOutfitControls(controlX, controlWidth, y);
        } else {
            if (selected != SHAPE) {
                addRenderableWidget(Button.builder(
                        Component.literal("Mesh: " + shapeFor(selected)),
                        button -> {
                            setShape(selected);
                            rebuildWidgets();
                        }).bounds(controlX, y, controlWidth, 20).build());
                y += 28;
            }
            for (BodyDimension dimension : BodyDimension.values()) {
                if (dimension.group() == selected) {
                    addRenderableWidget(new BodySlider(controlX, y, controlWidth, dimension));
                    y += 24;
                }
            }
        }

        int footerY = height - 30;
        int buttonW = (controlWidth - 8) / 3;
        addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
            AppearanceState.defaults();
            rebuildWidgets();
        }).bounds(controlX, footerY, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
            AppearanceState.cancelEdit();
            minecraft.setScreen(null);
        }).bounds(controlX + buttonW + 4, footerY, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
            AppearanceState.saveEdit();
            minecraft.setScreen(null);
        }).bounds(controlX + 2 * (buttonW + 4), footerY, buttonW, 20).build());
    }

    private void addOutfitControls(int x, int w, int y) {
        addRenderableWidget(Button.builder(Component.literal("Hat: " + AppearanceState.current().headwear()),
                b -> {
                    Appearance a = AppearanceState.current();
                    AppearanceState.setAppearance(new Appearance(a.head(), a.torso(), a.arms(), a.legs(),
                            AppearanceState.next(a.headwear(), Appearance.Headwear.values()),
                            a.top(), a.bottom(), a.feet()));
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Shirt: " + AppearanceState.current().top()),
                b -> { AppearanceState.cycleTop(); rebuildWidgets(); })
                .bounds(x, y + 26, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Pants: " + AppearanceState.current().bottom()),
                b -> { AppearanceState.cycleBottom(); rebuildWidgets(); })
                .bounds(x, y + 52, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Shoes: " + AppearanceState.current().feet()),
                b -> { AppearanceState.cycleFeet(); rebuildWidgets(); })
                .bounds(x, y + 78, w, 20).build());
    }

    private Appearance.Shape shapeFor(BodyDimension.Group group) {
        Appearance a = AppearanceState.current();
        return switch (group) {
            case HEAD -> a.head();
            case TORSO -> a.torso();
            case ARMS -> a.arms();
            case LEGS -> a.legs();
            default -> Appearance.Shape.STEVE;
        };
    }

    private void setShape(BodyDimension.Group group) {
        Appearance a = AppearanceState.current();
        Appearance.Shape next = AppearanceState.next(shapeFor(group), Appearance.Shape.values());
        AppearanceState.setAppearance(new Appearance(
                group == HEAD ? next : a.head(),
                group == TORSO ? next : a.torso(),
                group == ARMS ? next : a.arms(),
                group == LEGS ? next : a.legs(),
                a.headwear(), a.top(), a.bottom(), a.feet()));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.fill(3, 26, leftWidth, height - 5, 0xCC17202B);
        gui.fill(rightX, 26, width - 3, height - 5, 0xCC17202B);
        gui.fill(previewLeft, 26, previewRight, height - 5, 0x6630394A);

        gui.drawCenteredString(font, "MODULAR PLAYER", width / 2, 10, 0xFFFFFF);
        gui.drawString(font, "Body / Style", 13, 29, 0xA8C0DA);
        gui.drawString(font, selected == OUTFIT ? "Wardrobe" : "Dimensions",
                rightX + 8, 34, 0xA8C0DA);

        if (minecraft != null && minecraft.player != null && previewRight - previewLeft > 50) {
            int size = Math.max(15, Math.min(52, (height - 84) / 3)) + zoom;
            int bottom = height - 53;
            // Same entity used by the world renderer: live geometry and fitted clothing.
            Quaternionf view = Axis.ZP.rotationDegrees(180.0F)
                    .mul(Axis.XP.rotationDegrees(10.0F))
                    .mul(Axis.YP.rotationDegrees(rotation));
            InventoryScreen.renderEntityInInventory(gui, previewX, bottom, size,
                    view, null, minecraft.player);
        }
        gui.drawCenteredString(font, "Drag to rotate / Scroll to zoom", previewX,
                height - 26, 0xBAC9D4);
        gui.drawCenteredString(font, "Visual model only - gameplay size unchanged", width / 2,
                height - 9, 0x93A9B5);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    private boolean insidePreview(double x, double y) {
        return x >= previewLeft && x < previewRight && y >= 27 && y < height - 39;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && insidePreview(mouseX, mouseY)) {
            previewDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (previewDragging && button == 0) {
            rotation = (rotation + (float) dragX * 1.15F) % 360F;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && previewDragging) {
            previewDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        if (insidePreview(x, y)) {
            zoom = Math.max(-12, Math.min(34, zoom + (amount > 0 ? 3 : -3)));
            return true;
        }
        return super.mouseScrolled(x, y, amount);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        AppearanceState.cancelEdit();
        super.onClose();
    }
}
