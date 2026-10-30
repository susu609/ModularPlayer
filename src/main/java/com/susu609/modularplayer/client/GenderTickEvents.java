package com.susu609.modularplayer.client;

import com.susu609.modularplayer.ModularPlayerMod;
import com.susu609.modularplayer.client.physics.BreastPhysicsManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModularPlayerMod.ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GenderTickEvents {
    private GenderTickEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            BreastPhysicsManager.clear();
            return;
        }
        BreastPhysicsManager.tick(minecraft.player);
    }
}
