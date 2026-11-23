package com.lastbreath.client;

import com.lastbreath.LastBreath;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.lwjgl.openal.AL10;

/** Client-only state machine for the unconsciousness effect. */
public final class ClientEffects {
    private enum Phase { NONE, DEAD, WAKE }

    private static Phase phase = Phase.NONE;
    private static int deadTicks;
    private static int wakeTicks;
    private static TinnitusSound tinnitus;
    private static boolean gainTouched;

    private ClientEffects() {}

    // ---- packet entry point -------------------------------------------------

    public static void onPacket(boolean start) {
        if (start) beginDeath(); else beginWake();
    }

    private static void beginDeath() {
        Minecraft mc = Minecraft.getInstance();
        LastBreath.LOGGER.info("[LastBreath] client: blackout START received");
        restoreGain();
        stopTinnitus();
        phase = Phase.DEAD;
        deadTicks = 0;
        wakeTicks = 0;
        mc.getSoundManager().stop();           // cut every playing sound at once
        tinnitus = new TinnitusSound();
        mc.getSoundManager().play(tinnitus);
    }

    private static void beginWake() {
        LastBreath.LOGGER.info("[LastBreath] client: WAKE received (phase={})", phase);
        if (phase != Phase.DEAD) return;
        Minecraft mc = Minecraft.getInstance();
        stopTinnitus();
        phase = Phase.WAKE;
        wakeTicks = 0;
        applyGain(0.0F);
        if (mc.screen instanceof BlackoutScreen) mc.setScreen(null);
    }

    public static void reset() {
        stopTinnitus();
        restoreGain();
        phase = Phase.NONE;
        deadTicks = 0;
        wakeTicks = 0;
    }

    // ---- ticking ------------------------------------------------------------

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        // Fallback independent of ScreenEvent.Opening: never leave the vanilla death screen visible.
        if (mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen) {
            LastBreath.LOGGER.info("[LastBreath] client: vanilla DeathScreen replaced from tick");
            mc.setScreen(new BlackoutScreen());
        }
        if (phase == Phase.NONE) return;
        mc.options.hideGui = false;            // F1 would hide the black overlay
        if (phase == Phase.DEAD && mc.screen == null && mc.player != null) {
            mc.setScreen(new BlackoutScreen());
        }
        if (phase == Phase.DEAD) {
            deadTicks++;
        } else if (phase == Phase.WAKE) {
            wakeTicks++;
            if (wakeTicks >= LastBreath.WAKE_TICKS) {
                phase = Phase.NONE;
                restoreGain();
            }
        }
    }

    public static void renderTick(float partialTick) {
        if (phase == Phase.WAKE) {
            applyGain(baseGain() * smooth(wakeProgress(partialTick)));
        }
    }

    // ---- queries ------------------------------------------------------------

    public static boolean isDead() {
        return phase == Phase.DEAD;
    }

    /** True while normal game sounds must be suppressed. */
    public static boolean isSilenced() {
        if (phase == Phase.DEAD) return true;
        if (phase == Phase.NONE) {
            Minecraft mc = Minecraft.getInstance();
            return mc.player != null && mc.player.isDeadOrDying();
        }
        return false;
    }

    /** 0..1 tinnitus loudness factor. */
    public static float tinnitusLevel() {
        float attack = Mth.clamp(deadTicks / 15.0F, 0.0F, 1.0F);
        float release = 1.0F - Mth.clamp(
                (deadTicks - LastBreath.TINNITUS_FADE_START)
                        / (float) (LastBreath.RESPAWN_TICKS - LastBreath.TINNITUS_FADE_START), 0.0F, 1.0F);
        return attack * release;
    }

    /** 0..1 opacity of the black cover. */
    public static float blackAlpha(float partialTick) {
        switch (phase) {
            case DEAD:
                return 1.0F;
            case WAKE:
                return 1.0F - smooth(wakeProgress(partialTick));
            default:
                Minecraft mc = Minecraft.getInstance();
                return (mc.player != null && mc.player.isDeadOrDying()) ? 1.0F : 0.0F;
        }
    }

    // ---- helpers ------------------------------------------------------------

    private static float wakeProgress(float partialTick) {
        return Mth.clamp((wakeTicks + partialTick) / (float) LastBreath.WAKE_TICKS, 0.0F, 1.0F);
    }

    private static float smooth(float p) {
        return p * p * (3.0F - 2.0F * p);
    }

    private static float baseGain() {
        return Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
    }

    private static void applyGain(float gain) {
        try {
            AL10.alListenerf(AL10.AL_GAIN, gain);
            gainTouched = true;
        } catch (Throwable ignored) {
            // no audio device / AL not initialised
        }
    }

    private static void restoreGain() {
        if (!gainTouched) return;
        try {
            AL10.alListenerf(AL10.AL_GAIN, baseGain());
        } catch (Throwable ignored) {
        }
        gainTouched = false;
    }

    private static void stopTinnitus() {
        if (tinnitus != null) {
            tinnitus.finish();
            tinnitus = null;
        }
    }
}
