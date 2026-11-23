package com.lastbreath.server;

import com.lastbreath.LastBreath;
import com.lastbreath.network.Network;
import com.lastbreath.network.StatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LastBreath.MOD_ID)
public final class ServerEvents {
    /** UUID of unconscious player -> ticks left until automatic respawn. */
    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    private ServerEvents() {}

    private static void begin(ServerPlayer player) {
        PENDING.put(player.getUUID(), LastBreath.RESPAWN_TICKS);
        LastBreath.LOGGER.info("[LastBreath] {} lost consciousness, respawn in {} ticks", player.getGameProfile().getName(), LastBreath.RESPAWN_TICKS);
        Network.send(player, new StatePacket(true));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            begin(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // Player disconnected while dead: restart the sequence for them.
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)
                && player.getHealth() <= 0.0F) {
            begin(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PENDING.clear();
        // Instant respawn would let the client skip the 30 s sequence.
        MinecraftServer server = event.getServer();
        server.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(false, server);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (UUID id : new ArrayList<>(PENDING.keySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                PENDING.remove(id);
                continue;
            }
            if (!player.isDeadOrDying()) {
                // Respawned by something else: just wake the client.
                PENDING.remove(id);
                Network.send(player, new StatePacket(false));
                continue;
            }
            int left = PENDING.get(id) - 1;
            if (left > 0) {
                PENDING.put(id, left);
                continue;
            }
            PENDING.remove(id);
            ServerPlayer fresh = server.getPlayerList().respawn(player, false);
            fresh.connection.player = fresh;
            if (server.isHardcore()) {
                fresh.setGameMode(GameType.SPECTATOR);
            }
            Network.send(fresh, new StatePacket(false));
            LastBreath.LOGGER.info("[LastBreath] {} respawned", fresh.getGameProfile().getName());
        }
    }
}
