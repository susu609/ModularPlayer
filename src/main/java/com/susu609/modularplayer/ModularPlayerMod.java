package com.susu609.modularplayer;

import com.susu609.modularplayer.config.GenderConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

/** Forge 1.20.1 entrypoint; no legacy player-model replacement. */
@Mod(ModularPlayerMod.ID)
public final class ModularPlayerMod {
    public static final String ID = "modularplayer";

    public ModularPlayerMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GenderConfig.SPEC);
    }
}
