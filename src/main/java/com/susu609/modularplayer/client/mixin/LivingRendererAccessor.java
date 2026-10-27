package com.susu609.modularplayer.client.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Access to LivingEntityRenderer.model for replacing both player renderer variants. */
@Mixin(LivingEntityRenderer.class)
public interface LivingRendererAccessor {
    @Accessor("model")
    void modularplayer$setModel(EntityModel<?> model);
}
