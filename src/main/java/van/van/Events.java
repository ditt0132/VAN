package van.van;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Events implements Listener {
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == TeleportCause.COMMAND
                || e.getCause() == TeleportCause.PLUGIN
                || e.getCause() == TeleportCause.UNKNOWN)
            VariablesStorage.backLocations.put(e.getPlayer().getUniqueId(), e.getFrom());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        VariablesStorage.deathLocations.put(e.getPlayer().getUniqueId(), e.getPlayer().getLastDeathLocation());
        VariablesStorage.deathReasons.put(e.getPlayer().getUniqueId(), e.deathMessage());
    }
}
