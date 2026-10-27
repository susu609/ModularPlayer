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
        self.assertEqual(len(re.findall(r'^[ ]+[A-Z_]+\(Group\.', dimensions, re.M)), 31)
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
        self.assertIn('float[] vertical = new float[2]', physics)
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

    def test_local_only_scope(self):
        self.assertIn('ClientTarget.appliesTo(player)',
                      source('client/model/ModularPlayerModel.java'))

if __name__ == '__main__':
    unittest.main()
