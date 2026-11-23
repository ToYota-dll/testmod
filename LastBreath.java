package com.lastbreath;

import com.lastbreath.network.Network;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(LastBreath.MOD_ID)
public final class LastBreath {
    public static final String MOD_ID = "lastbreath";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Server: total time from death to automatic respawn (30 s). */
    public static final int RESPAWN_TICKS = 30 * 20;
    /** Client: tinnitus starts fading out at 25 s. */
    public static final int TINNITUS_FADE_START = 25 * 20;
    /** Client: fade-in of picture and sound after respawn (2 s). */
    public static final int WAKE_TICKS = 2 * 20;

    public LastBreath() {
        Network.register();
        LOGGER.info("[LastBreath] mod loaded, network channel registered");
    }
}
