package com.susu609.modularplayer.client.physics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayer;

/** Session-local physics state; no shared static spring channels. */
public final class BreastPhysicsManager {
    private static final Map<UUID, BothBreastsPhysics> STATES = new HashMap<>();

    private BreastPhysicsManager() {}

    public static void tick(LocalPlayer player) {
        STATES.computeIfAbsent(player.getUUID(), id -> new BothBreastsPhysics()).tick(player);
        // Foundation only renders local player. Remove stale world/session state.
        STATES.keySet().removeIf(id -> !id.equals(player.getUUID()));
    }

    public static BothBreastsPhysics get(UUID playerId) {
        return STATES.get(playerId);
    }

    public static void clear() {
        STATES.clear();
    }
}
