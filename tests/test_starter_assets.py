"""Pure-stdlib offline checks for PNG headers, resources, and component boundaries.

These are not Forge runtime tests.
"""
from pathlib import Path
import json
import re
import struct
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/susu609/modularplayer'
RES = ROOT / 'src/main/resources/assets/modularplayer'

class StarterAssetTests(unittest.TestCase):
    def test_png_dimensions_and_pixels(self):
        textures = list((RES / 'textures').rglob('*.png'))
        self.assertGreaterEqual(len(textures), 6)
        for file in textures:
            with self.subTest(file=file.name):
                data = file.read_bytes()
                self.assertTrue(data.startswith(b'\x89PNG\r\n\x1a\n'), file)
                self.assertEqual(data[12:16], b'IHDR')
                self.assertEqual(struct.unpack('>II', data[16:24]), (64, 64))
                self.assertIn(b'IDAT', data)
        self.assertEqual(len(list((RES / 'textures/outfit').glob('*.png'))), 5)

    def test_layout_registered(self):
        renderer = (JAVA / 'client/ClientRendererRegistration.java').read_text()
        geometry = (JAVA / 'client/model/BodyGeometry.java').read_text()
        for variant in ('STEVE', 'SLIM'):
            self.assertIn('event.registerLayerDefinition(' + variant, renderer)
        for mesh in ('head', 'body', 'right_arm', 'left_arm',
                     'right_leg', 'left_leg'):
            self.assertIn('"' + mesh + '"', geometry)

    def test_clothing_texture_refs_exist(self):
        source = (JAVA / 'client/model/ModularPlayerModel.java').read_text()
        for name in set(re.findall(r'outfit/[a-z_]+\.png', source)):
            self.assertTrue((RES / 'textures' / name).exists(), name)

    def test_json_syntax(self):
        for file in (ROOT / 'src/main/resources').rglob('*.json'):
            with self.subTest(file=file.name):
                json.loads(file.read_text())
        json.loads((ROOT / 'src/main/resources/pack.mcmeta').read_text())

if __name__ == '__main__':
    unittest.main()
