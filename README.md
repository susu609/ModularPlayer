# Modular Player — In-game Character Editor (v0.3 alpha)

**A full player avatar customization system, starting with Steve.** Minecraft Java 1.20.1 / Forge 47.4.10 / Java 17. Height is not the core goal: body geometry and clothing are independent, customizable systems.

## Open editor in-game

Press **P** while playing to open the **Modular Player** editor. Bedrock-inspired layout:

- **Left panel:** Head, Torso, Arms, Legs, Shape, Clothes
- **Center:** Preview of the *current local player* in 3D, drag with mouse to rotate, scroll to zoom
- **Right panel:** Change mesh preset, shape parameters, clothes
- **Save:** Apply changes and persist to Forge client config
- **Cancel / ESC:** Revert to the appearance from before opening the editor
- **Reset:** Restore default Steve body values in preview (press Save to persist)

Controls preview the same player renderer as the world. If your UI is very small, increase game window size or lower GUI Scale in Minecraft settings.

### Adjustable properties (15 sliders)

| Editor tab | Independent parameters |
|---|---|
| Head | width, height, depth |
| Torso | width, length, depth |
| Arms | width, length, depth |
| Legs | width, length, depth |
| Shape | chest/breast projection, buttock projection, hip width |
| Clothes | hat, shirt, pants, footwear; separate 64x64 texture atlases |

**Individual mesh presets** Steve / Wide can be chosen for Head, Torso, Arms and Legs independently. Chest and buttock volume default to **0** (Steve baseline) and are separate cubic meshes attached to the torso animation pivot; there are two independent left/right cubes within each anatomical group, driven by a common parameter. Shape protrusions use the clothing atlas when a top/bottom is equipped.

The editor is a **visual-only client prototype**. No hitbox, health, player height, eye height, camera, physics, server-side syncing, or remote-player skin is changed. Model-part scales do not yet support custom vertex edits or import/export Blockbench; clothing still uses the body silhouette and might clip at extreme values. Armor, cape, Elytra, first-person hands and modded animation compatibility need testing and dedicated rendering support. This is not a shipped mod release.

### Where to edit code

| Feature | Exact path in repository |
|---|---|
| Main editor screen, layout, drag/zoom, tabs/buttons | `src/main/java/com/susu609/modularplayer/client/gui/AvatarEditorScreen.java` |
| Slider UI widget | `src/main/java/com/susu609/modularplayer/client/gui/BodySlider.java` |
| All slider parameters and min/max/default | `src/main/java/com/susu609/modularplayer/appearance/BodyDimension.java` |
| Chest / buttock cube geometry | `src/main/java/com/susu609/modularplayer/client/model/BodyGeometry.java` |
| Scaling animation pivots, rendering shapes/outfits | `src/main/java/com/susu609/modularplayer/client/model/ModularPlayerModel.java` |
| Live appearance and Save/Cancel | `src/main/java/com/susu609/modularplayer/client/AppearanceState.java` |
| Persistent Forge client config | `src/main/java/com/susu609/modularplayer/ClientConfig.java` |
| Editor key binding (**P**) and quick switches (**B/V/N/M**) | `src/main/java/com/susu609/modularplayer/client/EditorHotkeys.java` |
| Wardrobe choices | `src/main/java/com/susu609/modularplayer/appearance/Appearance.java` |
| Steve body texture | `src/main/resources/assets/modularplayer/textures/body/steve.png` |
| Garment textures | `src/main/resources/assets/modularplayer/textures/outfit/` |

### Data flow

```text
P key -> AvatarEditorScreen -> AppearanceState (in-memory draft)
                                   |
                         BodyDimension values
                                   |
         PlayerRendererMixin -> ModularPlayerModel -> pose and geometry
                                   |
                               render clothes
                                   |
           Save -> ClientConfig -> config/modularplayer-client.toml
           Cancel -> restore prior session state
```

**Forge client config:** stored in `config/modularplayer-client.toml` under the current game directory. The `[appearance.dimensions]` section includes each slider key, such as `chest_volume`, `buttock_volume`, and `hip_width`.

## Build / tests

JDK 17, internet and Gradle 8.1.1 are required. Wrapper is not packaged in this starter.

```bash
gradle compileJava
gradle runClient
python -m unittest discover -s tests
```

CI is configured in `.github/workflows/compile.yml` to compile with Java 17. API references: [Forge screens](https://docs.minecraftforge.net/en/1.20.1/gui/screens/) and [Bedrock Character Creator](https://minecraft.wiki/w/Character_creator).

## Roadmap

1. Stable in-game preview and fully independent scaling sliders (this commit; compile/runtime verification still required).
2. Independent Blockbench-compatible BodyPart Registry (custom mesh geometry and bone pivots rather than cube presets).
3. Clothing Registry and garments with matching geometry, layering, offsets, UV mapping and anti-clipping.
4. Profiles, 3D thumbnail selection, presets, localized UI and import/export.
5. Multiplayer appearance sync, first-person arms, armor/elytra/cape integration and third-party animation support.
