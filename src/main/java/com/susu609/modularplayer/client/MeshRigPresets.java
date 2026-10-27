package com.susu609.modularplayer.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.susu609.modularplayer.appearance.MeshRig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Three explicit named slots as JSON files under the Minecraft config dir.
 * No paths are user-supplied; invalid files fail closed with IOException.
 * Loading changes only the in-memory rig; closing with Cancel still reverts it.
 */
public final class MeshRigPresets {
    private MeshRigPresets() {}

    private static Path slotPath(int slot) {
        if (slot < 1 || slot > 3) throw new IllegalArgumentException("Slot must be 1..3");
        return FMLPaths.CONFIGDIR.get().resolve("modularplayer")
                .resolve("rig-slot-" + slot + ".json");
    }

    public static void save(int slot, MeshRig rig) throws IOException {
        Path path = slotPath(slot);
        Files.createDirectories(path.getParent());
        JsonObject data = new JsonObject();
        data.addProperty("version", 1);
        JsonObject offsets = new JsonObject();
        for (MeshRig.Key key : MeshRig.keys()) {
            offsets.addProperty(key.configName(), rig.get(key));
        }
        data.add("offsets", offsets);
        Files.writeString(path, data.toString() + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public static MeshRig load(int slot) throws IOException {
        Path path = slotPath(slot);
        if (!Files.isRegularFile(path)) throw new IOException("Preset slot " + slot + " is empty");
        if (Files.size(path) > 32_768L) throw new IOException("Preset file exceeds 32 KB");
        try {
            JsonObject json = JsonParser.parseString(
                    Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!json.has("version") || json.get("version").getAsInt() != 1
                    || !json.has("offsets")) {
                throw new IOException("Unsupported mesh rig preset");
            }
            JsonObject offsets = json.getAsJsonObject("offsets");
            MeshRig rig = new MeshRig();
            for (MeshRig.Key key : MeshRig.keys()) {
                if (!offsets.has(key.configName())) continue;
                float offset = offsets.get(key.configName()).getAsFloat();
                if (!Float.isFinite(offset) || Math.abs(offset) > MeshRig.LIMIT) {
                    throw new IOException("Out of range: " + key.configName());
                }
                rig.set(key, offset);
            }
            return rig;
        } catch (IllegalStateException | NumberFormatException
                 | com.google.gson.JsonParseException error) {
            throw new IOException("Invalid preset JSON", error);
        }
    }
}
