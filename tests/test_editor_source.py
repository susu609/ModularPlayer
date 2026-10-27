"""Offline integrity checks for the character-editor prototype.

Only checks content invariants; not a substitute for Forge compilation or testing.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
PKG = ROOT / 'src/main/java/com/susu609/modularplayer'

def source(path):
    return (PKG / path).read_text(encoding='utf-8')

class EditorSourcesTest(unittest.TestCase):
    def test_preview_opened_with_p(self):
        src = source('client/EditorHotkeys.java')
        self.assertIn('GLFW.GLFW_KEY_P', src)
        self.assertIn('mc.setScreen(new AvatarEditorScreen())', src)

    def test_center_model_is_real_player(self):
        src = source('client/gui/AvatarEditorScreen.java')
        self.assertIn('InventoryScreen.renderEntityInInventory(', src)
        self.assertIn('minecraft.player', src)
        for section in ('HEAD', 'TORSO', 'ARMS', 'LEGS', 'CHEST', 'HIPS', 'BUTTOCKS', 'OUTFIT'):
            self.assertIn(section, src)
        for action in ('AppearanceState.cancelEdit()', 'AppearanceState.saveEdit()',
                       'AppearanceState.defaults()'):
            self.assertIn(action, src)

    def test_all_dimensions_represented_in_config_and_model(self):
        dimensions = source('appearance/BodyDimension.java')
        self.assertEqual(len(re.findall(r'^[ ]+[A-Z_]+\(Group\.', dimensions, re.M)), 35)
        self.assertIn('defineInRange(dimension.configName()', source('ClientConfig.java'))
        model = source('client/model/ModularPlayerModel.java')
        for name in ('CHEST_VOLUME', 'BUTTOCK_VOLUME', 'HIP_WIDTH',
                     'HEAD_WIDTH', 'TORSO_WIDTH', 'ARM_WIDTH', 'LEG_WIDTH'):
            self.assertIn(name, model)

    def test_chest_and_butt_geometry_use_surface_mesh(self):
        surface = source('client/model/AnatomySurface.java')
        self.assertIn('private static final int ANGLES = 16;', surface)
        self.assertIn('private static final int RINGS = 5;', surface)
        self.assertIn('for (int side : new int[]{-1, 1})', surface)
        self.assertIn('profile.depth(u, v, fullness)', surface)
        self.assertIn('CHEST_LEFT_SCALE', surface)
        self.assertIn('CHEST_RIGHT_SCALE', surface)
        self.assertIn('pose', surface.lower())

    def test_reference_chest_controls_and_motion(self):
        dimension_src = source('appearance/BodyDimension.java')
        model_src = source('client/model/ModularPlayerModel.java')
        motion = source('client/AnatomyMotion.java')
        surface_src = source('client/model/AnatomySurface.java')
        for name in ('CHEST_SEPARATION', 'CHEST_POSITION', 'CHEST_WIDTH',
                     'CHEST_HEIGHT', 'CHEST_SWAY', 'BUTTOCK_SEPARATION',
                     'BUTTOCK_POSITION', 'BUTTOCK_WIDTH', 'BUTTOCK_HEIGHT'):
            self.assertIn(name, dimension_src)
            self.assertIn(name, motion if name == 'CHEST_SWAY' else surface_src)
        self.assertIn('AnatomyMotion.tick(mc.player)', source('client/EditorHotkeys.java'))
        self.assertIn('AnatomySurface.render(', model_src)
        self.assertIn('AnatomySurface.render(', model_src)

    def test_breast_rig_2_preset_shape_and_independent_spring(self):
        profiles = source('appearance/BreastShapeProfile.java')
        presets = source('client/ChestPreset.java')
        editor = source('client/gui/AvatarEditorScreen.java')
        physics = source('client/AnatomyMotion.java')
        for profile in ('JENNY_INSPIRED', 'NATURAL', 'ROUND', 'ANGULAR'):
            self.assertIn(profile, profiles)
        for setting in ('CHEST_FULLNESS', 'CHEST_LIFT',
                        'CHEST_LEFT_SCALE', 'CHEST_RIGHT_SCALE',
                        'CHEST_FIRMNESS', 'CHEST_DAMPING'):
            self.assertIn(setting, source('appearance/BodyDimension.java'))
        self.assertIn('BreastShapeProfile.fromIndex(', editor)
        self.assertIn('chestAdvanced = !chestAdvanced', editor)
        self.assertIn('ChestPreset.values()[presetIndex].apply()', editor)
        self.assertIn('new BodySlider(', editor)
        self.assertIn('AnatomyMotion.reset()', presets)
        self.assertIn('float[][] vertical = new float[2][2]', physics)
        self.assertIn('float spring =', physics)
        self.assertIn('float drag =', physics)

    def test_isometric_camera_preview_presets(self):
        camera = source('client/gui/PreviewCamera.java')
        editor = source('client/gui/AvatarEditorScreen.java')
        self.assertIn('ISOMETRIC_ELEVATION = 35.2643897F', camera)
        for preset in ('ISOMETRIC_LEFT', 'ISOMETRIC_RIGHT',
                       'FRONT', 'SIDE', 'BACK', 'FREE'):
            self.assertIn(preset, camera)
        self.assertIn('Axis.YP.rotationDegrees(yaw())', camera)
        self.assertIn('Axis.XP.rotationDegrees(pitch())', camera)
        self.assertIn('camera.quaternion()', editor)
        self.assertIn('camera.nextPreset()', editor)
        self.assertIn('camera.drag(dragX, dragY)', editor)
        self.assertIn('GLFW.GLFW_KEY_I', editor)
        self.assertIn('camera.resetIsometric()', editor)

    def test_mesh_rig_editor_control_cage(self):
        mesh = source('appearance/MeshRig.java')
        geometry = source('client/model/AnatomySurface.java')
        screen = source('client/gui/AvatarEditorScreen.java')
        slider = source('client/gui/RigAxisSlider.java')
        state = source('client/AppearanceState.java')
        config = source('ClientConfig.java')
        for item in ('CENTER', 'UPPER', 'LOWER', 'INNER', 'OUTER',
                     'CHEST', 'BUTTOCKS', 'LEFT', 'RIGHT', 'X', 'Y', 'Z'):
            self.assertIn(item, mesh)
        self.assertIn('public Offset sample(', mesh)
        self.assertIn('if (radiusSquared >= 1F) return Offset.ZERO;', mesh)
        self.assertIn('MeshRig.Offset deform = rig.sample(', geometry)
        self.assertIn('float u =', geometry)
        self.assertIn('float v =', geometry)
        self.assertIn('MESH, EMOTES, OUTFIT', screen)
        self.assertIn('drawMeshRigMap(gui)', screen)
        self.assertIn('clickRigMap(mouseX, mouseY)', screen)
        self.assertIn('rigMapDragging', screen)
        self.assertIn('new RigAxisSlider(', screen)
        self.assertIn('axis == MeshRig.Axis.X ? -value : value', slider)
        self.assertIn('MeshRig rigBeforeEdit;', state)
        self.assertIn('rigBeforeEdit = rig().copy()', state)
        self.assertIn('rig = rigBeforeEdit;', state)
        self.assertIn('RIG_OFFSETS', config)
        self.assertIn('ClientConfig.save(current(), dimensions, rig())', state)

    def test_mesh_preset_slots_are_safe_and_explicit(self):
        code = source('client/MeshRigPresets.java')
        gui = source('client/gui/AvatarEditorScreen.java')
        self.assertIn('slot < 1 || slot > 3', code)
        self.assertIn('FMLPaths.CONFIGDIR.get()', code)
        self.assertIn('Files.size(path) > 32_768L', code)
        self.assertIn('data.addProperty("version", 1)', code)
        self.assertIn('Float.isFinite(offset)', code)
        self.assertIn('MeshRigPresets.save(rigSlot, AppearanceState.rig())', gui)
        self.assertIn('AppearanceState.setRig(MeshRigPresets.load(rigSlot))', gui)

    def test_whole_body_height_anchor_and_outfit(self):
        dimensions = source('appearance/BodyDimension.java')
        model = source('client/model/ModularPlayerModel.java')
        editor = source('client/gui/AvatarEditorScreen.java')
        self.assertIn('PLAYER_HEIGHT(Group.HEIGHT', dimensions)
        self.assertIn('HEIGHT, HEAD', dimensions)
        self.assertIn('stretchHeight(pose);', model)
        self.assertEqual(model.count('stretchHeight(pose);'), 2)
        self.assertIn('pose.translate(0F, 1.5F * (1F - height), 0F)', model)
        self.assertIn('pose.scale(1F, height, 1F)', model)
        self.assertIn('HEIGHT, HEAD', editor)

    def test_secondary_motion_responds_to_acceleration_for_both_parts(self):
        motion = source('client/AnatomyMotion.java')
        surface = source('client/model/AnatomySurface.java')
        dim = source('appearance/BodyDimension.java')
        for key in ('BUTTOCK_SWAY', 'BUTTOCK_FIRMNESS', 'BUTTOCK_DAMPING'):
            self.assertIn(key, dim)
            self.assertIn(key, motion)
        for phrase in ('velocity.subtract(previousVelocity)', 'wrapDegrees(yaw - previousYaw)',
                       'grounded && !previousGround', 'EmoteState.bodyShakeImpulse()',
                       'simulate(CHEST,', 'simulate(BUTTOCKS,',
                       'zero(part);', 'float drag ='):
            self.assertIn(phrase, motion)
        self.assertIn('AnatomyMotion.lateralPixels(rear, side)', surface)
        self.assertIn('AnatomyMotion.verticalPixels(rear, side)', surface)

    def test_emotes_live_editor_and_hotkey(self):
        emote = source('client/EmoteState.java')
        hotkeys = source('client/EditorHotkeys.java')
        model = source('client/model/ModularPlayerModel.java')
        editor = source('client/gui/AvatarEditorScreen.java')
        self.assertIn('WAVE("Wave",', emote)
        self.assertIn('BODY_SHAKE("Body shake",', emote)
        self.assertIn('GLFW.GLFW_KEY_G', hotkeys)
        self.assertIn('EmoteState.tick(mc.player)', hotkeys)
        self.assertIn('AnatomyMotion.tick(mc.player)', hotkeys)
        self.assertIn('applyEmote(ageInTicks)', model)
        self.assertIn('EmoteState.Emote.WAVE', model)
        self.assertIn('EmoteState.Emote.BODY_SHAKE', model)
        self.assertIn('addEmoteControls(', editor)
        self.assertIn('EmoteState.stop()', editor)
        self.assertIn('tabPage =', editor)

    def test_local_only_scope(self):
        self.assertIn('ClientTarget.appliesTo(player)',
                      source('client/model/ModularPlayerModel.java'))

if __name__ == '__main__':
    unittest.main()
