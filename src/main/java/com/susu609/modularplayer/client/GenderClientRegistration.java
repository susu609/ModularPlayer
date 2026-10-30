package com.susu609.modularplayer.client;

import com.susu609.modularplayer.ModularPlayerMod;
import com.susu609.modularplayer.client.model.BreastModel;
import com.susu609.modularplayer.client.render.GenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Register only a supplemental layer; preserve the vanilla player renderer. */
@Mod.EventBusSubscriber(modid = ModularPlayerMod.ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GenderClientRegistration {
    private GenderClientRegistration() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BreastModel.LAYER, BreastModel::createLayer);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinType : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skinType);
            if (renderer != null) {
                renderer.addLayer(new GenderLayer(renderer,
                        event.getEntityModels().bakeLayer(BreastModel.LAYER)));
            }
        }
    }
}
