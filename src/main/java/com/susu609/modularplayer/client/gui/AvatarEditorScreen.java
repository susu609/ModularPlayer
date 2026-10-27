package com.susu609.modularplayer.client.gui;

import com.susu609.modularplayer.appearance.Appearance;
import com.susu609.modularplayer.appearance.BodyDimension;
import com.susu609.modularplayer.appearance.MeshRig;
import com.susu609.modularplayer.client.MeshRigPresets;
import com.susu609.modularplayer.client.EmoteState;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import com.susu609.modularplayer.appearance.BreastShapeProfile;
import com.susu609.modularplayer.client.ChestPreset;
import com.susu609.modularplayer.client.AppearanceState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import static com.susu609.modularplayer.appearance.BodyDimension.Group.*;

/**
 * Bedrock-inspired editor: categories at left, current player preview at center,
 * parameter sliders and clothes at right. In-game, no external website required.
 */
public final class AvatarEditorScreen extends Screen {
    private static final BodyDimension.Group[] TABS =
            { HEIGHT, HEAD, TORSO, ARMS, LEGS, CHEST, HIPS, BUTTOCKS, MESH, EMOTES, OUTFIT };
    private static final String[] TAB_NAMES =
            { "Height", "Head", "Torso", "Arms", "Legs", "Chest", "Hips", "Buttocks", "Mesh Rig", "Emotes", "Clothes" };
    private BodyDimension.Group selected = CHEST;
    private boolean chestAdvanced;
    private int chestPage;
    private int tabPage;
    private int presetIndex = -1;
    private String guiPageLabel = "";
    private boolean previewDragging;
    private final PreviewCamera camera = new PreviewCamera();
    private MeshRig.Part rigPart = MeshRig.Part.CHEST;
    private RigAxisSlider.SideMode rigSide = RigAxisSlider.SideMode.BOTH;
    private MeshRig.Handle rigHandle = MeshRig.Handle.CENTER;
    private MeshRig.Axis rigAxis = MeshRig.Axis.Z;
    private int rigSlot = 1;
    private boolean rigMapDragging;
    private int rigMapX;
    private int rigMapY;
    private int rigMapRadius;
    private String rigStatus = "";
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

        // Fixed camera presets stay inside the center preview pane.
        int previewWidth = previewRight - previewLeft;
        if (previewWidth >= 85) {
            int viewButtonWidth = Math.min(144, previewWidth - 8);
            addRenderableWidget(Button.builder(
                    Component.literal("View: " + camera.label()), button -> {
                        camera.nextPreset();
                        rebuildWidgets();
                    }).bounds(previewX - viewButtonWidth / 2, 31, viewButtonWidth, 20).build());
        }

        // Scroll-like pagination keeps all categories reachable at low GUI heights.
        int tabsPerPage = Math.max(2, (height - 112) / 23);
        int pages = (TABS.length + tabsPerPage - 1) / tabsPerPage;
        tabPage = Math.min(tabPage, pages - 1);
        for (int i = tabPage * tabsPerPage;
             i < Math.min(TABS.length, (tabPage + 1) * tabsPerPage); i++) {
            final BodyDimension.Group tab = TABS[i];
            String text = (selected == tab ? "> " : "") + TAB_NAMES[i];
            addRenderableWidget(Button.builder(Component.literal(text), button -> {
                selected = tab;
                chestPage = 0;
                rebuildWidgets();
            }).bounds(8, 40 + (i - tabPage * tabsPerPage) * 23,
                    leftWidth - 12, 20).build());
        }
        if (pages > 1) {
            int footer = height - 55;
            int half = (leftWidth - 16) / 2;
            addRenderableWidget(Button.builder(Component.literal("<"),
                    b0 -> { tabPage = (tabPage + pages - 1) % pages; rebuildWidgets(); })
                    .bounds(8, footer, half, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"),
                    b0 -> { tabPage = (tabPage + 1) % pages; rebuildWidgets(); })
                    .bounds(12 + half, footer, half, 20).build());
        }

        int controlX = rightX + 7;
        int controlWidth = rightWidth - 14;
        int y = 54;
        if (selected == OUTFIT) {
            addOutfitControls(controlX, controlWidth, y);
        } else if (selected == CHEST) {
            addChestControls(controlX, controlWidth, y);
        } else if (selected == MESH) {
            addMeshRigControls(controlX, controlWidth, y);
        } else if (selected == EMOTES) {
            addEmoteControls(controlX, controlWidth, y);
        } else {
            if (selected == HEAD || selected == TORSO || selected == ARMS || selected == LEGS) {
                addRenderableWidget(Button.builder(
                        Component.literal("Mesh: " + shapeFor(selected)),
                        button -> {
                            setShape(selected);
                            rebuildWidgets();
                        }).bounds(controlX, y, controlWidth, 20).build());
                y += 28;
            }
            List<BodyDimension> controls = new ArrayList<>();
            for (BodyDimension dimension : BodyDimension.values()) {
                if (dimension.group() == selected) controls.add(dimension);
            }
            int pageSize = Math.max(1, (height - y - 58) / 24);
            int pageCount = Math.max(1, (controls.size() + pageSize - 1) / pageSize);
            chestPage = Math.min(chestPage, pageCount - 1);
            for (int i = chestPage * pageSize;
                 i < Math.min(controls.size(), (chestPage + 1) * pageSize); i++) {
                addRenderableWidget(new BodySlider(controlX,
                        y + (i - chestPage * pageSize) * 24,
                        controlWidth, controls.get(i)));
            }
            if (pageCount > 1) {
                int nav = Math.max(24, (controlWidth - 64) / 2);
                int navY = height - 56;
                addRenderableWidget(Button.builder(Component.literal("<"),
                        b0 -> { chestPage = (chestPage + pageCount - 1) % pageCount; rebuildWidgets(); })
                        .bounds(controlX, navY, nav, 18).build());
                addRenderableWidget(Button.builder(Component.literal(">"),
                        b0 -> { chestPage = (chestPage + 1) % pageCount; rebuildWidgets(); })
                        .bounds(controlX + controlWidth - nav, navY, nav, 18).build());
                guiPageLabel = (chestPage + 1) + "/" + pageCount;
            } else {
                guiPageLabel = "";
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

    /**
     * Short, adjustable pages prevent sliders overlapping Save/Cancel at
     * Minecraft's smaller GUI resolutions. All changes preview immediately.
     */
    private void addChestControls(int x, int w, int y) {
        BreastShapeProfile profile = BreastShapeProfile.fromIndex(
                AppearanceState.get(BodyDimension.CHEST_STYLE));
        addRenderableWidget(Button.builder(Component.literal("Shape: " + profile.label()),
                button -> {
                    int style = (profile.ordinal() + 1) % BreastShapeProfile.values().length;
                    AppearanceState.set(BodyDimension.CHEST_STYLE, style);
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal(
                        presetIndex < 0 ? "Load preset..." : "Preset: " + ChestPreset.values()[presetIndex].label()),
                button -> {
                    presetIndex = (presetIndex + 1) % ChestPreset.values().length;
                    ChestPreset.values()[presetIndex].apply();
                    chestPage = 0;
                    rebuildWidgets();
                }).bounds(x, y + 24, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal(
                        chestAdvanced ? "Controls: Advanced" : "Controls: Basic"),
                button -> {
                    chestAdvanced = !chestAdvanced;
                    chestPage = 0;
                    rebuildWidgets();
                }).bounds(x, y + 48, w, 20).build());

        BodyDimension[] controls = chestAdvanced
                ? new BodyDimension[]{
                    BodyDimension.CHEST_WIDTH, BodyDimension.CHEST_HEIGHT,
                    BodyDimension.CHEST_POSITION, BodyDimension.CHEST_LEFT_SCALE,
                    BodyDimension.CHEST_RIGHT_SCALE, BodyDimension.CHEST_FIRMNESS,
                    BodyDimension.CHEST_DAMPING }
                : new BodyDimension[]{
                    BodyDimension.CHEST_VOLUME, BodyDimension.CHEST_FULLNESS,
                    BodyDimension.CHEST_LIFT, BodyDimension.CHEST_SEPARATION,
                    BodyDimension.CHEST_SWAY };
        int sliderStartY = y + 77;
        int pageY = height - 57;
        int pageSize = Math.max(1, (pageY - sliderStartY - 5) / 24);
        int totalPages = (controls.length + pageSize - 1) / pageSize;
        chestPage = Math.min(chestPage, totalPages - 1);
        for (int i = chestPage * pageSize; i < Math.min(controls.length,
                (chestPage + 1) * pageSize); i++) {
            addRenderableWidget(new BodySlider(x, sliderStartY
                    + (i - chestPage * pageSize) * 24, w, controls[i]));
        }
        if (totalPages > 1) {
            int navWidth = Math.max(24, (w - 68) / 2);
            addRenderableWidget(Button.builder(Component.literal("<"),
                    b -> { chestPage = (chestPage + totalPages - 1) % totalPages; rebuildWidgets(); })
                    .bounds(x, pageY, navWidth, 18).build());
            addRenderableWidget(Button.builder(Component.literal(">"),
                    b -> { chestPage = (chestPage + 1) % totalPages; rebuildWidgets(); })
                    .bounds(x + w - navWidth, pageY, navWidth, 18).build());
            guiPageLabel = (chestPage + 1) + " / " + totalPages;
        } else {
            guiPageLabel = "";
        }
    }

    /** Weighted surface-cage editor: select and drag handles in the 2D map. */
    private void addMeshRigControls(int x, int w, int y) {
        addRenderableWidget(Button.builder(Component.literal("Mesh: " + rigPart.name()),
                b -> {
                    rigPart = rigPart == MeshRig.Part.CHEST
                            ? MeshRig.Part.BUTTOCKS : MeshRig.Part.CHEST;
                    rigHandle = MeshRig.Handle.CENTER;
                    rebuildWidgets();
                }).bounds(x, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Side: " + rigSide.name()),
                b -> { rigSide = rigSide.next(); rebuildWidgets(); })
                .bounds(x, y + 25, w, 20).build());
        // A flat Steve surface has no polygons to pick visually. Provide an
        // explicit, reversible preview action rather than silently modifying
        // the avatar when the user opens this tab.
        BodyDimension projection = rigPart == MeshRig.Part.CHEST
                ? BodyDimension.CHEST_VOLUME : BodyDimension.BUTTOCK_VOLUME;
        boolean flat = AppearanceState.get(projection) < .001;
        addRenderableWidget(Button.builder(Component.literal(
                        flat ? "Show surface (draft)" : "Axis: " + rigAxis.name()),
                b -> {
                    if (flat) {
                        AppearanceState.set(projection, .85);
                    } else {
                        rigAxis = MeshRig.Axis.values()[(rigAxis.ordinal() + 1)
                                % MeshRig.Axis.values().length];
                    }
                    rebuildWidgets();
                }).bounds(x, y + 50, w, 20).build());

        int sliderY = Math.max(y + 104, height - 108);
        rigMapX = x + w / 2;
        rigMapY = (y + 78 + sliderY - 12) / 2;
        rigMapRadius = Math.max(0, Math.min(35, (sliderY - (y + 80)) / 2 - 5));
        addRenderableWidget(new RigAxisSlider(x, sliderY, w,
                rigPart, rigHandle, rigAxis, rigSide));

        addRenderableWidget(Button.builder(Component.literal("Preset slot: " + rigSlot),
                b -> { rigSlot = rigSlot % 3 + 1; rebuildWidgets(); })
                .bounds(x, height - 81, w, 20).build());
        int buttonWidth = (w - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal("Save preset"), b -> saveRigSlot())
                .bounds(x, height - 56, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Load preset"), b -> loadRigSlot())
                .bounds(x + buttonWidth + 4, height - 56, buttonWidth, 20).build());
    }

    private void saveRigSlot() {
        try {
            MeshRigPresets.save(rigSlot, AppearanceState.rig());
            rigStatus = "Saved slot " + rigSlot;
        } catch (IOException ex) {
            rigStatus = "Save failed: " + ex.getMessage();
        }
    }

    private void loadRigSlot() {
        try {
            AppearanceState.setRig(MeshRigPresets.load(rigSlot));
            rigStatus = "Loaded slot " + rigSlot + " (press Save to apply)";
            rebuildWidgets();
        } catch (IOException ex) {
            rigStatus = "Load failed: " + ex.getMessage();
        }
    }

    private void addEmoteControls(int x, int w, int y) {
        addRenderableWidget(Button.builder(Component.literal("Wave / Vay tay"),
                b -> EmoteState.start(EmoteState.Emote.WAVE, minecraft.player))
                .bounds(x, y, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Body Shake"),
                b -> EmoteState.start(EmoteState.Emote.BODY_SHAKE, minecraft.player))
                .bounds(x, y + 26, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop emote"),
                b -> EmoteState.stop())
                .bounds(x, y + 52, w, 20).build());
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
        if (!guiPageLabel.isEmpty() && selected != MESH && selected != OUTFIT
                && selected != EMOTES) {
            gui.drawCenteredString(font, guiPageLabel, rightX + rightWidth / 2,
                    height - 53, 0xBAC9D4);
        }

        if (selected == MESH) {
            drawMeshRigMap(gui);
        }
        if (selected == EMOTES) {
            gui.drawString(font, "Now: " + EmoteState.current().label(),
                    rightX + 8, 141, 0xFFA9D9C9);
        }

        if (minecraft != null && minecraft.player != null && previewRight - previewLeft > 50) {
            int size = Math.max(15, Math.min(52, (height - 84) / 3)) + zoom;
            int bottom = height - 53;
            // A camera-only isometric pose preserves our exact live world avatar.
            InventoryScreen.renderEntityInInventory(gui, previewX, bottom, size,
                    camera.quaternion(), null, minecraft.player);
        }
        gui.drawCenteredString(font, "Drag: rotate | Scroll: zoom | I: ISO", previewX,
                height - 26, 0xBAC9D4);
        gui.drawCenteredString(font, "Visual model only - gameplay size unchanged", width / 2,
                height - 9, 0x93A9B5);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    private MeshRig.Side mapSide() {
        return rigSide == RigAxisSlider.SideMode.RIGHT
                ? MeshRig.Side.RIGHT : MeshRig.Side.LEFT;
    }

    private int handleX(MeshRig.Handle handle) {
        MeshRig.Offset offset = AppearanceState.rig().get(rigPart, mapSide(), handle);
        return rigMapX + Math.round(handle.u(mapSide()) * rigMapRadius * .84F
                + offset.x() * 2.8F);
    }

    private int handleY(MeshRig.Handle handle) {
        MeshRig.Offset offset = AppearanceState.rig().get(rigPart, mapSide(), handle);
        return rigMapY + Math.round(handle.v() * rigMapRadius * .84F
                + offset.y() * 2.8F);
    }

    /** Compact control-cage diagram beside the live 3D preview. */
    private void drawMeshRigMap(GuiGraphics gui) {
        int r = rigMapRadius;
        if (r < 12) {
            gui.drawCenteredString(font, "Increase GUI height for rig map",
                    rigMapX, rigMapY, 0xFFC199);
            return;
        }
        for (int degree = 0; degree < 360; degree += 6) {
            double a = Math.toRadians(degree);
            int px = rigMapX + (int) Math.round(r * Math.cos(a));
            int py = rigMapY + (int) Math.round(r * Math.sin(a));
            gui.fill(px, py, px + 2, py + 2, 0xFF617A95);
        }
        gui.fill(rigMapX - r, rigMapY, rigMapX + r, rigMapY + 1, 0x55667799);
        gui.fill(rigMapX, rigMapY - r, rigMapX + 1, rigMapY + r, 0x55667799);
        for (MeshRig.Handle handle : MeshRig.Handle.values()) {
            int px = handleX(handle), py = handleY(handle);
            int color = handle == rigHandle ? 0xFFFFDD72 : 0xFF72C7E8;
            gui.fill(px - 3, py - 3, px + 4, py + 4, 0xFF10202E);
            gui.fill(px - 2, py - 2, px + 3, py + 3, color);
        }
        gui.drawCenteredString(font, rigHandle.label() + " / " + rigAxis.name(),
                rigMapX, Math.max(126, rigMapY - r - 12), 0xFFC8DCE9);
        if (!rigStatus.isEmpty()) {
            gui.drawCenteredString(font, rigStatus, rigMapX,
                    Math.min(height - 116, rigMapY + r + 6), 0xFFB5DEB7);
        }
    }

    private boolean clickRigMap(double mouseX, double mouseY) {
        if (selected != MESH || rigMapRadius < 12) return false;
        MeshRig.Handle nearest = null;
        double nearestSq = 100.0;
        for (MeshRig.Handle handle : MeshRig.Handle.values()) {
            double dx = mouseX - handleX(handle);
            double dy = mouseY - handleY(handle);
            double d2 = dx * dx + dy * dy;
            if (d2 < nearestSq) {
                nearest = handle;
                nearestSq = d2;
            }
        }
        if (nearest == null) return false;
        rigHandle = nearest;
        rigMapDragging = true;
        rebuildWidgets();
        return true;
    }

    private boolean insidePreview(double x, double y) {
        return x >= previewLeft && x < previewRight && y >= 27 && y < height - 39;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && clickRigMap(mouseX, mouseY)) return true;
        if (button == 0 && insidePreview(mouseX, mouseY)) {
            previewDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (rigMapDragging && button == 0) {
            float step = .055F;
            RigAxisSlider.write(rigPart, rigHandle, MeshRig.Axis.X, rigSide,
                    RigAxisSlider.read(rigPart, rigHandle, MeshRig.Axis.X, rigSide)
                            + (float) dragX * step);
            RigAxisSlider.write(rigPart, rigHandle, MeshRig.Axis.Y, rigSide,
                    RigAxisSlider.read(rigPart, rigHandle, MeshRig.Axis.Y, rigSide)
                            + (float) dragY * step);
            return true;
        }
        if (previewDragging && button == 0) {
            camera.drag(dragX, dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && rigMapDragging) {
            rigMapDragging = false;
            rebuildWidgets();
            return true;
        }
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

    /** I restores the default isometric view without modifying avatar settings. */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_I) {
            camera.resetIsometric();
            rebuildWidgets();
            return true;
        }
        if (selected == MESH && keyCode == GLFW.GLFW_KEY_R) {
            AppearanceState.rig().reset(rigPart);
            rigStatus = "Reset " + rigPart.name() + " mesh (draft)";
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
