import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/susu609/modularplayer"


class GenderFoundationTests(unittest.TestCase):
    def test_additive_layer_replaces_player_mixin(self):
        renderer = (JAVA / "client/GenderClientRegistration.java").read_text()
        self.assertIn("renderer.addLayer(new GenderLayer", renderer)
        self.assertFalse((JAVA / "client/mixin/PlayerRendererMixin.java").exists())
        self.assertFalse((ROOT / "src/main/resources/modularplayer.mixins.json").exists())

    def test_independent_springs_and_interpolation(self):
        physics = (JAVA / "client/physics/BothBreastsPhysics.java").read_text()
        spring = (JAVA / "client/physics/BreastPhysics.java").read_text()
        self.assertIn("new BreastPhysics()", physics)
        self.assertIn("left.tick(", physics)
        self.assertIn("right.tick(", physics)
        self.assertIn("Mth.lerp(", spring)

    def test_vanilla_skin_and_model_animation(self):
        layer = (JAVA / "client/render/GenderLayer.java").read_text()
        self.assertIn("body.translateAndRotate(pose)", layer)
        self.assertIn("player.getSkinTextureLocation()", layer)
        self.assertIn("PlayerModelPart.JACKET", layer)

    def test_no_legacy_editor(self):
        self.assertFalse((JAVA / "client/gui/AvatarEditorScreen.java").exists())
        self.assertFalse((JAVA / "client/model/AnatomySurface.java").exists())


if __name__ == "__main__":
    unittest.main()
