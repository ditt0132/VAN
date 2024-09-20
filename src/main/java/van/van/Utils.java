package van.van;

import io.papermc.paper.util.Tick;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    public static final String DATE_WITHOUT_SECONDS = "yyyy.MM.dd HH:mm";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String TIME_WITHOUT_SECONDS = "HH:mm";

    public static Duration getPlaytime(OfflinePlayer p) {
         // Actually means ticks played so needs convert!
        return Tick.of(p.getStatistic(Statistic.PLAY_ONE_MINUTE));
    }
    public static void sendLocalChat(Player sender, String message) {
        double radius = 300.0;
        Location senderLoc = sender.getLocation();
        List<Player> nearbyPlayers = senderLoc.getNearbyPlayers(radius).stream()
                .filter(p -> p != sender)
                .collect(Collectors.toList());

        // 예시:
        //   보내는사람: [지역] <simple_bird> test
        //   받는사람: [4m] <simple_bird> test
        for (Player player : nearbyPlayers) {
            player.sendMessage(Component.text("["+player.getLocation().distance(senderLoc)+"] ").color(NamedTextColor.DARK_GREEN)
                    .append(Component.text(message)));
        }
    }

    public static void sendGlobalChat(Player sender, String message) {
        Bukkit.broadcast(Component.text(message));
    }
    public static String locationToString(Location loc) {
        Location l = loc.toBlockLocation();
        return l.x()+", "+l.y()+", "+l.z();
    }
    public static String boolToChar(boolean bool) {
        if (bool) return "T";
        else return "F";
    }
}
