package es.nkmem.da.movefreq;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import es.nkmem.da.movefreq.packethooks.PositionLookHook;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class MoveFreqPlugin extends JavaPlugin {
    @Getter
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        new PositionLookHook(this).hook();
    }

    @Override
    public void onDisable() {
        protocolManager.removePacketListeners(this);
    }
}
