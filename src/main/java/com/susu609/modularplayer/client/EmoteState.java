package com.susu609.modularplayer.client;

import net.minecraft.client.player.LocalPlayer;
import java.util.UUID;

/**
 * Client-only emote state, advanced by the same client tick as animation.
 * Emotes are animation overlays, not chat emoji, server actions or gameplay buffs.
 */
public final class EmoteState {
    public enum Emote {
        NONE("Stop", 0),
        WAVE("Wave", 66),
        BODY_SHAKE("Body shake", 90);

        private final String label;
        private final int durationTicks;
        Emote(String label, int durationTicks) {
            this.label = label;
            this.durationTicks = durationTicks;
        }
        public String label() { return label; }
        public int durationTicks() { return durationTicks; }
    }

    private static UUID playerId;
    private static Emote current = Emote.NONE;
    private static int elapsed;

    private EmoteState() {}

    public static void start(Emote emote, LocalPlayer player) {
        if (player == null || emote == Emote.NONE) {
            stop();
            return;
        }
        playerId = player.getUUID();
        current = emote;
        elapsed = 0;
    }

    public static void stop() {
        current = Emote.NONE;
        elapsed = 0;
        playerId = null;
    }

    public static void tick(LocalPlayer player) {
        if (player == null || (playerId != null && !playerId.equals(player.getUUID()))) {
            stop();
            return;
        }
        if (current != Emote.NONE && ++elapsed >= current.durationTicks()) {
            stop();
        }
    }

    public static Emote current() { return current; }
    public static int elapsed() { return elapsed; }
    public static boolean playing(Emote emote) { return current == emote; }

    /** Fade the pose in and out, to avoid a discontinuity when it stops. */
    public static float envelope() {
        if (current == Emote.NONE) return 0F;
        return Math.min(1F, Math.min(elapsed / 6F,
                (current.durationTicks() - elapsed) / 9F));
    }

    /** Channels matching the animated torso pose in ModularPlayerModel. */
    public static float torsoSway() {
        return playing(Emote.BODY_SHAKE)
                ? (float) Math.sin(elapsed * .74F) * .14F * envelope() : 0F;
    }

    public static float torsoTwist() {
        return playing(Emote.BODY_SHAKE)
                ? (float) Math.sin(elapsed * .74F) * .18F * envelope() : 0F;
    }

    public static float torsoLift() {
        return playing(Emote.BODY_SHAKE)
                ? (float) Math.cos(elapsed * 1.48F) * .16F * envelope() : 0F;
    }

    /** Legacy emote force, kept for compatibility. */
    public static float bodyShakeImpulse() {
        if (!playing(Emote.BODY_SHAKE)) return 0F;
        return (float) Math.sin(elapsed * 1.0F) * envelope();
    }
}
