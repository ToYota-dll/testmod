package com.lastbreath.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Replaces the vanilla death screen: pure black, no text, no buttons. */
public final class BlackoutScreen extends Screen {
    public BlackoutScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int a = Math.round(ClientEffects.blackAlpha(partialTick) * 255.0F);
        if (a > 0) g.fill(0, 0, this.width, this.height, a << 24);
    }

    @Override
    public void tick() {
        if (!ClientEffects.isDead() && this.minecraft != null && this.minecraft.player != null
                && !this.minecraft.player.isDeadOrDying()) {
            this.minecraft.setScreen(null);
        }
    }
}
