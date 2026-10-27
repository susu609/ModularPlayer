# Modular Player — In-game Character Editor (v0.3 alpha)

**A full player avatar customization system, starting with Steve.** Minecraft Java 1.20.1 / Forge 47.4.10 / Java 17. Height is not the core goal: body geometry and clothing are independent, customizable systems.

## Open editor in-game

Press **P** while playing to open the **Modular Player** editor. Bedrock-inspired layout:

- **Left panel:** Head, Torso, Arms, Legs, Chest, Hips, Buttocks, Clothes
- **Center:** Preview of the *current local player* in 3D, drag with mouse to rotate, scroll to zoom
- **Right panel:** Change mesh preset, shape parameters, clothes
- **Save:** Apply changes and persist to Forge client config
- **Cancel / ESC:** Revert to the appearance from before opening the editor
- **Reset:** Restore default Steve body values in preview (press Save to persist)

Controls preview the same player renderer as the world. If your UI is very small, increase game window size or lower GUI Scale in Minecraft settings.

### Adjustable properties (24 sliders)

| Editor tab | Independent parameters |
|---|---|
| Head | width, height, depth |
| Torso | width, length, depth |
| Arms | width, length, depth |
| Legs | width, length, depth |
| Chest | projection, separation (px), position (px), width, height, optional soft-motion strength |
| Hips | hip width |
| Buttocks | projection, separation (px), position (px), width, height |
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


## Gender Mod inspiration (v0.4)

Reference: [Female Gender Mod](https://github.com/FemaleGenderMod/FemaleGenderMod) (LGPL-3.0), especially its design of **two separate anatomy pieces** with adjustable separation, vertical positioning and depth, player-specific controls, and optional motion. We did not copy its source code, models, textures, or binaries. These controls and the small spring-damper are written independently for ModularPlayer.

- Chest and buttock groups remain independent geometry parts (no global model height change).
- Default = flat Steve with chest/buttock projection 0 and physics disabled.
- Each skin/clothing overlay is passed through the **same** geometry transformation to stay aligned.
- `CHEST_SWAY` is client-local, defaults to zero, updates once per client tick. It does not move bones used for hitboxes.
- Bedrock-inspired screen now has dedicated Chest, Hips and Buttocks tabs so the controls stay visible.
- Optional physics and garment fitting are an alpha prototype. Character animation, armor clipping, UV stretching, renderer conflicts, and movement feel should be checked in a running client.

Reference mod specifically does not aim to remodel vanilla hips or buttocks; those features are designed independently here.


## v0.5: procedural surface geometry inspired by Jenny / Fapcraft

Research references:
- [Jenny Fabric port](https://github.com/Nomadvorga/jenny-mod): source geometry `src/main/resources/assets/sexmod/geo/jenny/jennynude.geo.json` and `jennydressed.geo.json`.
- [Blockbench rigging guide](https://github.com/bernie-g/geckolib/wiki/Making-Your-Models-%28Blockbench%29): how planes, cubes, groups, bone pivots and rotations work.

Inspection showed that Jenny's model uses rotated *zero-thickness UV planes*, grouped into independent anatomy bones rather than one rectangular solid (the inspected port contains 7 and 6 geometry primitives in its nude left and right breast bones, respectively; the clothed preview groups contain 13 each). ModularPlayer does **not** import or redistribute these assets.

Our independent implementation is `src/main/java/com/susu609/modularplayer/client/model/AnatomySurface.java`:
- Two separate convex patches per body group, drawn with **5 concentric rings × 16 angular segments** using Minecraft's `VertexConsumer`. This is not an exact match to Jenny.
- Procedural depth profile and UVs are mapped to the front/back Steve 64×64 torso atlas.
- The same mesh function runs for bare-skin and separate clothing textures, with an outward shell offset.
- Chest controls: size, projection, vertical position, separation and optional damped lateral/vertical motion. Buttocks share geometry math but do not get the soft motion yet.
- No GeckoLib dependency is added; vanilla player animations are preserved. No hitbox or first-person changes.

**Known limitations:** surface geometry is still an alpha approximation, uses the torso UV atlas rather than custom material layouts, and must be visually tested from all angles. Continuous garment fitting, custom clothing geometry, UV stretching, armor layers, first-person hands and Blockbench model import remain planned work. The geometry samples can be replaced by a JSON/Blockbench mesh provider in a future renderer module.


## v0.6: Breast Rig 2.0 — model shape and physics controlled separately

Goal: **use Jenny's chest silhouette as an artistic reference without reusing the Jenny geometry or source**. The implementation follows broadly used character customization patterns such as blend/morph shapes and spring joints, but operates on the existing Minecraft `VertexConsumer` surface mesh.

- `appearance/BreastShapeProfile.java` contains four independent parametric curves: **Jenny-inspired, Natural, Round, Angular**. Shape profile does not force size or jiggle.
- `client/ChestPreset.java` offers **Jenny-inspired / Natural / Round / Soft Motion / Steve** starting points. Only chest controls change. Default world avatar remains flat Steve.
- `appearance/BodyDimension.java` adds editable `chest_fullness`, `chest_lift`, `chest_left_scale`, `chest_right_scale`, `chest_firmness`, `chest_damping` and a discrete `chest_style`. Values persist automatically with the existing `ClientConfig` dimension registry.
- `client/model/AnatomySurface.java` deforms left/right lobes separately. UVs and normals are generated for the procedural profile, and both skin and outfit use the same geometry procedure.
- `client/AnatomyMotion.java` has two independently damped channels. Motion strength, spring stiffness and damping are separate, bounded controls. Zero motion disables the solver.
- `client/gui/AvatarEditorScreen.java`: open with **P**, Chest tab has a **shape selector, preset selector**, Basic/Advanced button and **pagination** so controls fit typical GUI sizes. Basic: size, fullness, lift, spacing, soft motion; Advanced: width, height, position, left/right scaling, stiffness, damping.

**Limits:** no GeckoLib or native per-vertex mesh import, no breast/cloth collision simulation, no real cloth draping, and no third-party assets. Top garment currently shares the deformed surface with a small shell offset, not a volumetric tailored garment. Build/test still cannot verify the preview in-game without an actual client run.

References for implementation philosophy:
- [Unity deformable mesh and morph targets](https://docs.unity.cn/6000.1/Documentation/Manual/mesh-data-deformable-meshes.html)
- [VRM SpringBone 1.0 spec](https://github.com/vrm-c/vrm-specification/blob/master/specification/VRMC_springBone-1.0/README.md)


## v0.7: Isometric preview camera

The Character Editor opened by **P** now starts with a **3/4 isometric-style** preview angle. The new **View** button in the center preview pane cycles through **ISO Left, ISO Right, Front, Side and Back**.

- Isometric angle: **yaw ±45°**, **elevation 35.264°**, based on standard isometric axis rotations. Uses the existing Minecraft 1.20.1 `InventoryScreen.renderEntityInInventory` GUI rendering path.
- Drag the preview with the left mouse button to switch to **Free** camera, rotating yaw and pitch independently. Pitch is clamped to keep the player visible.
- Scroll to zoom as before.
- Press **I** while this screen is open to return instantly to ISO Left.
- These are **editor-only camera poses**. Player rotation, world view, physics, model proportions, wardrobe and persisted avatar profiles remain unchanged.
- No offscreen image exporter and no separate custom projection-matrix override is included. This is a camera-angle trial; exact pixel-perfect orthographic render fidelity still requires an in-game visual check.

Modify: `src/main/java/com/susu609/modularplayer/client/gui/PreviewCamera.java` for camera rotations and `AvatarEditorScreen.java` for the view selector/UI.


## v0.8: Mesh Rig Editor (weighted control cage)

**Open: P → Mesh Rig** in the Minecraft 1.20.1 client Character Editor.

This milestone edits the **procedurally generated chest and buttock surfaces** using weighted 3D control-point deformation, not imported hardcoded model data.

- Choose **Mesh: CHEST** or **BUTTOCKS** in the right panel.
- Choose **Side: BOTH / LEFT / RIGHT**. BOTH mirrors horizontal (X) offsets for symmetry; vertical (Y) and depth (Z) offsets apply equally.
- The small **2D control-cage map in the right panel** contains **5 selectable handles per lobe**: Center, Upper, Lower, Inner, Outer. Click a handle, then drag it horizontally/vertically to adjust X/Y. The avatar preview in the center updates immediately.
- The **Axis X/Y/Z** button selects which numerical offset the nearby slider controls. Select **Z** to adjust depth; its range is ±2.5 model pixels.
- Select a **Preset slot 1/2/3**, then **Save preset** to write `config/modularplayer/rig-slot-N.json`. **Load preset** reads the file into the current unsaved draft.
- **Save** in the screen footer persists the current avatar and all control-point offsets in Forge's `config/modularplayer-client.toml`; **Cancel/ESC** restores the in-memory state from before opening the screen (it does not delete previously saved JSON presets).
- **Reset** returns the whole avatar and control cage to the Steve defaults (press Save to persist).

The cage lives at `appearance/MeshRig.java`, stores 2 body parts × 2 sides × 5 handles × 3 axes = **60 editable scalar values**, and smoothly interpolates offsets across each surface. Values taper to zero at the outer rim to reduce holes. The same deformed vertices are used for bare body and garment rendering.

Main source paths under `src/main/java/com/susu609/modularplayer/`:

| File | Purpose |
| --- | --- |
| `appearance/MeshRig.java` | Control points, safe ranges, interpolation |
| `client/gui/AvatarEditorScreen.java` | Mesh Rig tab, point selection and 2D drag controls |
| `client/gui/RigAxisSlider.java` | X/Y/Z slider and mirrored selection mode |
| `client/model/AnatomySurface.java` | Applies deformations to actual rendered mesh vertices and recalculates normals |
| `client/MeshRigPresets.java` | JSON preset save/load (3 fixed, validated file names) |
| `client/AppearanceState.java` | Transactional live preview and Save/Cancel |
| `ClientConfig.java` | Forge config persistence for 60 axes |

**Important limitations:** The selectable handle diagram is **2D in the right pane**, while the actual 3D avatar stays in the center. It is *not yet* 3D handle picking directly on the model or a complete vertex-level/Blockbench editor. The rig currently covers chest/buttock surface meshes; head, torso, arms and legs still use their previous dimension sliders and preset geometry. If a surface's projection is zero (flat Steve), increase Chest Size or Buttocks Projection before evaluating the rig deformation. Runtime visual QA is needed to confirm UI scaling, UV alignment and clothing clipping.


### Rig editor visibility / reset

- A flat Steve shape has projection 0. In Mesh Rig, **Show surface (draft)** sets the currently chosen chest/buttock projection to 0.85 for editing. This is a normal draft change, so **Cancel/ESC restores the original flat shape**.
- Press **R** while Mesh Rig is selected to reset only the active mesh's 3D control-point offsets. The full avatar and other mesh parts are preserved until the global Reset button is chosen.


## v0.9 — Secondary Motion, Height and Emotes

**P → Height**: use the `Whole-body height` slider (default **1.00×**, range **0.70–1.70×**). The same Y-scale is applied at the model-space foot anchor to both avatar geometry and fitted clothing, including head, limbs, chest and buttocks. This changes **visual player height only**; hitbox, eye height, reach, shadows, vanilla armor and other third-party layers are **not** automatically resized. For full physical height in multiplayer, server-owned entity dimensions and networking would require a separate feature.

**P → Chest / Buttocks**: customize soft-body secondary motion. The two anatomical groups each use bounded, independent **spring stiffness**, **damping**, and **strength** with left/right channels. Physics is driven at 20 ticks/second by changes in player velocity, yaw rate, landing impulse and the body-shake emote; stationary characters settle back to neutral. This is a lightweight kinematic spring simulation, **not** volumetric finite-element body physics or collision-based cloth physics. Chest uses the existing sliders in **Basic/Advanced**. Buttocks gets three additional sliders that can be reached with its paginated category controls.

**P → Emotes**: preview **Wave** (one arm waves, approximately 3.3s), **Body shake** (torso/shoulder/leg motion, approximately 4.5s), and **Stop**. Emote animations are local-only overlays on top of vanilla walking and held-item poses. Press **G** in gameplay to cycle Wave → Body shake → Stop. Motion and animation can be seen in the same 3D player preview. These are gestures, **not chat emoji**.

Editor left-side tabs are paginated for smaller GUI heights; navigate with < and >. Use Save to persist your body settings, or Cancel/ESC to restore the draft. Emote playback itself is a temporary action, not a saved outfit setting.

Implementation under `src/main/java/com/susu609/modularplayer/`:
- `appearance/BodyDimension.java`: `PLAYER_HEIGHT` and the Buttocks physics controls.
- `client/AnatomyMotion.java`: separate acceleration-driven springs for both groups.
- `client/EmoteState.java`: timed gestures and body-shake impulses.
- `client/model/ModularPlayerModel.java`: foot-anchored whole-body scaling and skeleton pose overlays.
- `client/model/AnatomySurface.java`: live motion offsets applied equally to bare surfaces and outfits.
- `client/gui/AvatarEditorScreen.java`: Height / Emotes tabs, paginated controls.
- `client/EditorHotkeys.java`: emote G key, state and physics tick.

**Known limitations:** no native animation timeline editor; other clients cannot see this local player's emotes, and this version doesn't change the physical/collision height. Forge compilation and source tests do not prove rendering in a real client, so test the editor in-game at front/side/isometric views before calling the visuals final.
