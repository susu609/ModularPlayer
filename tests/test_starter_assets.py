"""Offline checks: textures, JSON, and component boundaries (not Forge compilation)."""
from pathlib import Path
from PIL import Image
import json
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT/'src/main/java/com/susu609/modularplayer'
RES = ROOT/'src/main/resources/assets/modularplayer'

class StarterAssetTests(unittest.TestCase):
    def test_png_dimensions_and_nontransparent(self):
        for file in (RES/'textures').rglob('*.png'):
            with self.subTest(file=file.name):
                with Image.open(file) as img:
                    self.assertEqual(img.size, (64,64))
                    self.assertIsNotNone(img.convert('RGBA').getbbox())
        self.assertEqual(len(list((RES/'textures/outfit').glob('*.png'))), 5)

    def test_layout_registered(self):
        renderer = (JAVA/'client/ClientRendererRegistration.java').read_text()
        geometry = (JAVA/'client/model/BodyGeometry.java').read_text()
        for v in ('STEVE','SLIM'):
            self.assertIn('event.registerLayerDefinition('+v,renderer)
        for mesh in ('head','body','right_arm','left_arm','right_leg','left_leg'):
            self.assertIn('"'+mesh+'"', geometry)

    def test_clothing_texture_refs_exist(self):
        source = (JAVA/'client/model/ModularPlayerModel.java').read_text()
        for name in set(re.findall(r'outfit/[a-z_]+\.png', source)):
            self.assertTrue((RES/'textures'/name).exists(), name)

    def test_json_syntax(self):
        for file in (ROOT/'src/main/resources').rglob('*.json'):
            with self.subTest(file=file.name):
                json.loads(file.read_text())
        json.loads((ROOT/'src/main/resources/pack.mcmeta').read_text())

if __name__ == '__main__':
    unittest.main()
