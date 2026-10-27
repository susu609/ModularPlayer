package com.susu609.modularplayer.client;

import com.susu609.modularplayer.ModularPlayerMod;
import com.susu609.modularplayer.client.layer.ModularClothingLayer;
import com.susu609.modularplayer.client.model.BodyGeometry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModularPlayerMod.ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientRendererRegistration {
    public static final ModelLayerLocation STEVE = new ModelLayerLocation(
            new ResourceLocation(ModularPlayerMod.ID, "player_steve"), "main");
    public static final ModelLayerLocation SLIM = new ModelLayerLocation(
            new ResourceLocation(ModularPlayerMod.ID, "player_slim"), "main");

    private ClientRendererRegistration() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(STEVE, () -> BodyGeometry.create(false));
        event.registerLayerDefinition(SLIM, () -> BodyGeometry.create(true));
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new ModularClothingLayer(renderer));
        }
    }
}
