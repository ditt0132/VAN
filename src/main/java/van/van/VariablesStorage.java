package van.van;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class VariablesStorage {
    public static Map<UUID, Location> backLocations = new HashMap<>();
    public static Map<UUID, Location> deathLocations = new HashMap<>();
    public static Map<UUID, Component> deathReasons = new HashMap<>();
    public static Map<UUID, Boolean> localChat = new HashMap<>();
}
