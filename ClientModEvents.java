package com.lastbreath.client;

import com.lastbreath.LastBreath;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LastBreath.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("blackout", (gui, g, partialTick, w, h) -> {
            int a = Math.round(ClientEffects.blackAlpha(partialTick) * 255.0F);
            if (a > 0) g.fill(0, 0, w, h, a << 24);
        });
    }
}
