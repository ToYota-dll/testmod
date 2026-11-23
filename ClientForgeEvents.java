package com.lastbreath.client;

import com.lastbreath.LastBreath;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LastBreath.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof DeathScreen) {
            LastBreath.LOGGER.info("[LastBreath] client: DeathScreen replaced in ScreenEvent.Opening");
            event.setNewScreen(new BlackoutScreen());
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() != null && !(event.getSound() instanceof TinnitusSound)
                && ClientEffects.isSilenced()) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) ClientEffects.clientTick();
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) ClientEffects.renderTick(event.renderTickTime);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientEffects.reset();
    }
}
