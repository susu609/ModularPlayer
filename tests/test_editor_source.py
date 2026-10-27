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
        self.assertEqual(len(re.findall(r'^[ ]+[A-Z_]+\(Group\.', dimensions, re.M)), 24)
        self.assertIn('defineInRange(dimension.configName()', source('ClientConfig.java'))
        model = source('client/model/ModularPlayerModel.java')
        for name in ('CHEST_VOLUME', 'BUTTOCK_VOLUME', 'HIP_WIDTH',
                     'HEAD_WIDTH', 'TORSO_WIDTH', 'ARM_WIDTH', 'LEG_WIDTH'):
            self.assertIn(name, model)

    def test_chest_and_butt_geometry_own_cubes(self):
        src = source('client/model/BodyGeometry.java')
        for name in ('chest_left','chest_right','glute_left','glute_right'):
            self.assertIn('"' + name + '"',src)

    def test_reference_chest_controls_and_motion(self):
        dimension_src = source('appearance/BodyDimension.java')
        model_src = source('client/model/ModularPlayerModel.java')
        motion = source('client/AnatomyMotion.java')
        for name in ('CHEST_SEPARATION', 'CHEST_POSITION', 'CHEST_WIDTH',
                     'CHEST_HEIGHT', 'CHEST_SWAY', 'BUTTOCK_SEPARATION',
                     'BUTTOCK_POSITION', 'BUTTOCK_WIDTH', 'BUTTOCK_HEIGHT'):
            self.assertIn(name, dimension_src)
            self.assertIn(name, model_src if name != 'CHEST_SWAY' else motion)
        self.assertIn('AnatomyMotion.tick(mc.player)', source('client/EditorHotkeys.java'))
        self.assertIn('float clothingMultiplier', model_src)
        self.assertIn('renderAnatomy(', model_src)

    def test_local_only_scope(self):
        self.assertIn('ClientTarget.appliesTo(player)',
                      source('client/model/ModularPlayerModel.java'))

if __name__ == '__main__':
    unittest.main()
