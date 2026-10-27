package com.susu609.modularplayer.client.mixin;

import com.susu609.modularplayer.ModularPlayerMod;
import com.susu609.modularplayer.client.ClientRendererRegistration;
import com.susu609.modularplayer.client.ClientTarget;
import com.susu609.modularplayer.client.model.ModularPlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    private static final ResourceLocation BODY = new ResourceLocation(ModularPlayerMod.ID,
            "textures/body/steve.png");

    @Inject(method = "<init>", at = @At("TAIL"))
    private void modularplayer$setup(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        ModularPlayerModel model = new ModularPlayerModel(context.bakeLayer(
                slim ? ClientRendererRegistration.SLIM : ClientRendererRegistration.STEVE), slim);
        ((LivingRendererAccessor) (Object) this).modularplayer$setModel(model);
    }

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"), cancellable = true)
    private void modularplayer$texture(AbstractClientPlayer player,
                                       CallbackInfoReturnable<ResourceLocation> cir) {
        if (ClientTarget.appliesTo(player)) cir.setReturnValue(BODY);
    }
}
