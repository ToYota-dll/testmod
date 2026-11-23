package com.lastbreath.network;

import com.lastbreath.client.ClientEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> one client. start=true: unconsciousness begins; start=false: wake up. */
public final class StatePacket {
    private final boolean start;

    public StatePacket(boolean start) {
        this.start = start;
    }

    public static void encode(StatePacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.start);
    }

    public static StatePacket decode(FriendlyByteBuf buf) {
        return new StatePacket(buf.readBoolean());
    }

    public static void handle(StatePacket p, Supplier<NetworkEvent.Context> ctx) {
        final boolean start = p.start;
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEffects.onPacket(start)));
        ctx.get().setPacketHandled(true);
    }
}
