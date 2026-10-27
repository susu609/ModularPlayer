package com.susu609.modularplayer.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.susu609.modularplayer.ModularPlayerMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Prototype direct editor: torso, shirt, pants and boots switch without restart. */
public final class EditorHotkeys {
    private static final String CATEGORY = "key.categories.modularplayer";
    private static final KeyMapping BODY = key("torso", GLFW.GLFW_KEY_B);
    private static final KeyMapping TOP = key("shirt", GLFW.GLFW_KEY_V);
    private static final KeyMapping BOTTOM = key("pants", GLFW.GLFW_KEY_N);
    private static final KeyMapping FEET = key("boots", GLFW.GLFW_KEY_M);

    private EditorHotkeys() {}

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.modularplayer." + name,
                InputConstants.Type.KEYSYM, code, CATEGORY);
    }

    @Mod.EventBusSubscriber(modid = ModularPlayerMod.ID, value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registry {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(BODY);
            event.register(TOP);
            event.register(BOTTOM);
            event.register(FEET);
        }
    }

    @Mod.EventBusSubscriber(modid = ModularPlayerMod.ID, value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class TickListener {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;
            if (BODY.consumeClick()) {
                AppearanceState.cycleTorso();
                show(mc, "Torso: " + AppearanceState.current().torso());
            }
            if (TOP.consumeClick()) {
                AppearanceState.cycleTop();
                show(mc, "Shirt: " + AppearanceState.current().top());
            }
            if (BOTTOM.consumeClick()) {
                AppearanceState.cycleBottom();
                show(mc, "Pants: " + AppearanceState.current().bottom());
            }
            if (FEET.consumeClick()) {
                AppearanceState.cycleFeet();
                show(mc, "Boots: " + AppearanceState.current().feet());
            }
        }

        private static void show(Minecraft mc, String message) {
            mc.player.displayClientMessage(Component.literal(message), true);
        }
    }
}
