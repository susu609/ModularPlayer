package com.susu609.modularplayer.client;

import com.susu609.modularplayer.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

public final class ClientTarget {
    private ClientTarget() {}

    public static boolean appliesTo(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        return ClientConfig.ENABLED.get() && minecraft.player != null
                && minecraft.player.getUUID().equals(player.getUUID());
    }
}
