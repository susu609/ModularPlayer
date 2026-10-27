package com.susu609.modularplayer;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ModularPlayerMod.ID)
public final class ModularPlayerMod {
    public static final String ID = "modularplayer";

    public ModularPlayerMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }
}
