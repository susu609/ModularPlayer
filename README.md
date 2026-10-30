# ModularPlayer — Gender foundation (Forge 1.20.1)

This branch is a **clean rebuild**, based on the *architecture* of Wildfire's Female Gender Mod. It does **not** vendor Wildfire source code.

## Status

- Stages 1–2: legacy character editor, procedural body shapes, clothing editor, emotes, rigging, and mixins removed.
- Vanilla Steve/Alex player renderers and player skins preserved.
- New local-player-only breast render layer, with separate sides and 64x64 UV-mapped cubes.
- Configurable size, shape scales, separation and vertical offset.
- Basic per-player secondary spring state with interpolated lateral/vertical motion and twist.
- No GUI, armor breast shell, remote player sync or third-party animation bridge **yet**.
- Chest armor currently hides the breast layer rather than clipping through it.

## Run

Requires Java 17, Minecraft 1.20.1, Forge 47.4.10.

```sh
gradle compileJava
gradle runClient
python3 -m unittest discover -s tests
```

The repo currently uses the installed Gradle in CI (no Gradle wrapper is committed).

## Initial testing

1. Start a new client instance, enter a world and view third-person.
2. Edit `config/modularplayer-client.toml`, then restart or reload config:
   - `gender.selection = "FEMALE"`
   - `gender.breasts.size = 0.65`
   - `gender.physics.enabled = true`
   - `gender.physics.strength = 0.55`
3. Test walking, sprinting, jumping, landing and turning.
4. Check jacket overlay; wearing a chestplate temporarily hides breast geometry.
5. Toggle `gender.selection = "MALE"` to confirm removal.

**Important:** These are TOML section keys: the exact layout nests `breasts` and `physics` under `gender`. In the generated file edit the `size` key in `[gender.breasts]` and `strength` in `[gender.physics]`.

## Where to edit

| Purpose | File |
|---|---|
| Breast shape, geometry, UV | `src/main/java/com/susu609/modularplayer/client/model/BreastModel.java` |
| Player body attachment, per-side transforms | `src/main/java/com/susu609/modularplayer/client/render/GenderLayer.java` |
| Spring solver | `src/main/java/com/susu609/modularplayer/client/physics/BreastPhysics.java` |
| Walking/jumping/turning forces | `src/main/java/com/susu609/modularplayer/client/physics/BothBreastsPhysics.java` |
| Local config | `src/main/java/com/susu609/modularplayer/config/GenderConfig.java` |

## Next stages

1. Shape presets and safe additional geometry models.
2. Animation bone-motion tracking and more accurate springs.
3. Clothing/armor shells.
4. New in-game customization UI.
5. Per-player data and multiplayer sync.
6. Runtime verification and performance tests.

## Reference and licensing

Architectural inspiration: https://github.com/FemaleGenderMod/FemaleGenderMod (LGPL-3.0).
This implementation is written independently under this repo's MIT license. Do not import third-party source without separately meeting its license requirements.
