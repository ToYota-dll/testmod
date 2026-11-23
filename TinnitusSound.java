package com.lastbreath.client;

import com.lastbreath.LastBreath;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class TinnitusSound extends AbstractTickableSoundInstance {
    private static final float BASE_VOLUME = 0.30F;

    public TinnitusSound() {
        super(SoundEvent.createVariableRangeEvent(new ResourceLocation(LastBreath.MOD_ID, "tinnitus")),
                SoundSource.MASTER, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    @Override
    public void tick() {
        if (!ClientEffects.isDead()) {
            this.stop();
            return;
        }
        this.volume = BASE_VOLUME * ClientEffects.tinnitusLevel();
    }

    public void finish() {
        this.stop();
    }
}
