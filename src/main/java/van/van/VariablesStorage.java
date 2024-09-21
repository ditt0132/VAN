package van.van;

import me.ryanhamshire.GriefPrevention.Claim;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VariablesStorage {
    private static File dataFile = new File("plugins/VAN/data.yml");
    private static YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
    public static Map<UUID, Location> backLocations = new HashMap<>();
    public static Map<UUID, Location> deathLocations = new HashMap<>();
    public static Map<UUID, Component> deathReasons = new HashMap<>();
    public static Map<UUID, Boolean> localChat = new HashMap<>();
    public static Map<Player, Player> lastWhisper = new HashMap<>();

    public static void loadData() {
        if (!dataFile.exists()) return;
        try {
            for (String key : config.getConfigurationSection("backLocations").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                backLocations.put(uuid, config.getLocation("backLocations." + key));
            }

            for (String key : config.getConfigurationSection("deathLocations").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                deathLocations.put(uuid, config.getLocation("deathLocations." + key));
            }

            for (String key : config.getConfigurationSection("lastRewardedTimes").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                ClaimManager.lastRewardedTimes.put(uuid, (Duration) config.get("lastRewardedTimes." + key));
            }

            for (String key : config.getConfigurationSection("claimCount").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                ClaimManager.claimCount.put(uuid, config.getInt("claimCount." + key));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        for (UUID uuid : backLocations.keySet()) {
            config.set("backLocations." + uuid.toString(), backLocations.get(uuid));
        }

        for (UUID uuid : deathLocations.keySet()) {
            config.set("deathLocations." + uuid.toString(), deathLocations.get(uuid));
        }

        for (UUID uuid : ClaimManager.lastRewardedTimes.keySet()) {
            config.set("lastRewardedTimes."+uuid.toString(), ClaimManager.lastRewardedTimes.get(uuid));
        }

        for (UUID uuid : ClaimManager.claimCount.keySet()) {
            config.set("claimCount."+uuid.toString(), ClaimManager.claimCount.get(uuid));
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
