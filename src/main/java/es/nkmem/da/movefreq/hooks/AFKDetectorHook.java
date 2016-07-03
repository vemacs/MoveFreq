package es.nkmem.da.movefreq.hooks;

import com.comphenix.packetwrapper.WrapperPlayClientLook;
import com.comphenix.packetwrapper.WrapperPlayClientPosition;
import com.comphenix.packetwrapper.WrapperPlayClientPositionLook;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import es.nkmem.da.movefreq.MoveFreqPlugin;
import es.nkmem.da.movefreq.packets.LookPacket;
import es.nkmem.da.movefreq.packets.PositionLookPacket;
import es.nkmem.da.movefreq.packets.PositionPacket;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class AFKDetectorHook {
    private final MoveFreqPlugin plugin;

    private Map<UUID, Long> lastUpdate = new ConcurrentHashMap<>();
    private LoadingCache<UUID, MovementData> movements = CacheBuilder.newBuilder()
            .build(new CacheLoader<UUID, MovementData>() {
                @Override
                public MovementData load(UUID uuid) throws Exception {
                    return new MovementData();
                }
            });

    @Data
    public class MovementData {
        private LookPacket look;
        private PositionPacket position;
        private PositionLookPacket positionLook;
    }

    private void updateLast(Player p) {
        lastUpdate.put(p.getUniqueId(), System.currentTimeMillis());
    }

    private boolean isAFK(Player p) {
        return (System.currentTimeMillis() - lastUpdate.get(p.getUniqueId())) > (144 * 1000);
    }

    public void hook() {
        Bukkit.getOnlinePlayers().forEach(this::updateLast);

        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerChat(AsyncPlayerChatEvent event) {
                updateLast(event.getPlayer());
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
                updateLast(event.getPlayer());
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerInteract(PlayerInteractEvent event) {
                updateLast(event.getPlayer());
            }

            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                updateLast((Player) event.getWhoClicked());
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerJoin(PlayerJoinEvent event) {
                updateLast(event.getPlayer());
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerQuit(PlayerQuitEvent event) {
                lastUpdate.remove(event.getPlayer().getUniqueId());
                movements.invalidate(event.getPlayer().getUniqueId());
            }
        }, plugin);

        ProtocolManager protocolManager = plugin.getProtocolManager();

        // TODO: DRY
        PacketAdapter.AdapterParameteters posLook = new PacketAdapter.AdapterParameteters().clientSide()
                .types(PacketType.Play.Client.POSITION_LOOK).listenerPriority(ListenerPriority.LOWEST).plugin(plugin);
        protocolManager.addPacketListener(new PacketAdapter(posLook) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientPositionLook wrapper = new WrapperPlayClientPositionLook(event.getPacket());
                PositionLookPacket packet = new PositionLookPacket(wrapper);
                Player p = event.getPlayer();

                MovementData data = movements.getUnchecked(p.getUniqueId());
                if (!packet.equals(data.getPositionLook())) {
                    data.setPositionLook(packet);
                    updateLast(p);
                } else if (isAFK(p)) {
                    // plugin.getLogger().info("AFK and cancelling posLook for " + p.getName());
                    event.setCancelled(true);
                }
            }
        });

        PacketAdapter.AdapterParameteters look = new PacketAdapter.AdapterParameteters().clientSide()
                .types(PacketType.Play.Client.LOOK).listenerPriority(ListenerPriority.LOWEST).plugin(plugin);
        protocolManager.addPacketListener(new PacketAdapter(look) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientLook wrapper = new WrapperPlayClientLook(event.getPacket());
                LookPacket packet = new LookPacket(wrapper);
                Player p = event.getPlayer();

                MovementData data = movements.getUnchecked(p.getUniqueId());
                if (!packet.equals(data.getLook())) {
                    data.setLook(packet);
                    updateLast(p);
                } else if (isAFK(p)) {
                    // plugin.getLogger().info("AFK and cancelling look for " + p.getName());
                    event.setCancelled(true);
                }
            }
        });

        PacketAdapter.AdapterParameteters pos = new PacketAdapter.AdapterParameteters().clientSide()
                .types(PacketType.Play.Client.POSITION).listenerPriority(ListenerPriority.LOWEST).plugin(plugin);
        protocolManager.addPacketListener(new PacketAdapter(pos) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientPosition wrapper = new WrapperPlayClientPosition(event.getPacket());
                PositionPacket packet = new PositionPacket(wrapper);
                Player p = event.getPlayer();

                MovementData data = movements.getUnchecked(p.getUniqueId());
                if (!packet.equals(data.getPosition())) {
                    data.setPosition(packet);
                    updateLast(p);
                } else if (isAFK(p)) {
                    // plugin.getLogger().info("AFK and cancelling pos for " + p.getName());
                    event.setCancelled(true);
                }
            }
        });

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int afk = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isAFK(p)) afk++;
           }
            plugin.getLogger().info("Number of AFK players: " + afk);
        }, 600, 600);
    }
}
