package van.van;

import me.ryanhamshire.GriefPrevention.Claim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import java.util.stream.Collectors;

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
            System.out.println("Loading data");
            for (String key : config.getConfigurationSection("backLocations").getKeys(false)) {
                System.out.println("bl");
                UUID uuid = UUID.fromString(key);
                backLocations.put(uuid, config.getLocation("backLocations." + key));
                System.out.println(key+" "+backLocations.get(uuid));
            }

            for (String key : config.getConfigurationSection("deathLocations").getKeys(false)) {
                System.out.println("dl");
                UUID uuid = UUID.fromString(key);
                deathLocations.put(uuid, config.getLocation("deathLocations." + key));
                System.out.println(key+" "+deathLocations.get(uuid));
            }

            for (String key : config.getConfigurationSection("deathReasons").getKeys(false)) {
                System.out.println("dr");
                UUID uuid = UUID.fromString(key);
                deathReasons.put(uuid, JSONComponentSerializer.json().deserialize(config.getString("deathReasons." + key,
                        "{\"text\":\"메시지 로드 실패\",\"color\":\"gray\"}")));
                System.out.println(key+" "+ PlainTextComponentSerializer.plainText().serialize(deathReasons.get(uuid)));
            }

            for (String key : config.getConfigurationSection("lastRewardedTimes").getKeys(false)) {
                System.out.println("lrt");
                UUID uuid = UUID.fromString(key);
                ClaimManager.lastRewardedTimes.put(uuid, Duration.ofSeconds(config.getLong("lastRewardedTimes." + key)));
                System.out.println(key+" "+ClaimManager.lastRewardedTimes.get(uuid));
            }

            for (String key : config.getConfigurationSection("claimCount").getKeys(false)) {
                System.out.println("cc");
                UUID uuid = UUID.fromString(key);
                ClaimManager.claimCount.put(uuid, config.getInt("claimCount." + key));
                System.out.println(key+" "+ClaimManager.claimCount.get(uuid));
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        System.out.println("Saving data");
        config.createSection("backLocations", backLocations);
        config.createSection("deathLocations", deathLocations);
        config.createSection("deathReasons", deathReasons.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> JSONComponentSerializer.json().serialize(e.getValue()))
        ));
        config.createSection("lastRewardedTimes", ClaimManager.lastRewardedTimes.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toSeconds())
        ));
        config.createSection("claimCount", ClaimManager.claimCount);

        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
//
//        for (UUID uuid : backLocations.keySet()) {
//            config.set("backLocations." + uuid.toString(), backLocations.get(uuid));
//        }
//
//        for (UUID uuid : deathLocations.keySet()) {
//            config.set("deathLocations." + uuid.toString(), deathLocations.get(uuid));
//        }
//
//        for (UUID uuid : ClaimManager.lastRewardedTimes.keySet()) {
//            config.set("lastRewardedTimes."+uuid.toString(), ClaimManager.lastRewardedTimes.get(uuid).toSeconds());
//        }
//
//        for (UUID uuid : ClaimManager.claimCount.keySet()) {
//            config.set("claimCount."+uuid.toString(), ClaimManager.claimCount.get(uuid));
//        }
//
//        try {
//            config.save(dataFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
