package van.van;

import io.papermc.paper.util.Tick;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {
    public static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    public static final String DATE_WITHOUT_SECONDS = "yyyy.MM.dd HH:mm";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String TIME_WITHOUT_SECONDS = "HH:mm";

    public static Duration getPlaytime(OfflinePlayer p) {
         // Actually means ticks played so needs convert!
        return Tick.of(p.getStatistic(Statistic.PLAY_ONE_MINUTE));
    }
    public static void sendLocalChat(Player sender, Component message) {
        double radius = 300.0;
        Location senderLoc = sender.getLocation();
        List<Player> nearbyPlayers = senderLoc.getNearbyPlayers(radius).stream()
                .filter(p -> p != sender)
                .toList();

        // 예시:
        //   보내는사람: [지역] <simple_bird> test
        //   받는사람: [4m] <simple_bird> test
        for (Player player : nearbyPlayers) {
            player.sendMessage(VAN.mm.deserialize("<dark_green>[%.0fm] <white><%s> "
                    .formatted(senderLoc.distance(player.getLocation()), sender.getName())).append(message.color(NamedTextColor.WHITE)));
        }
        sender.sendMessage(VAN.mm.deserialize("<dark_green>[지역] <white><%s> ".formatted(sender.getName())).append(message.color(NamedTextColor.WHITE)));
        LoggerFactory.getLogger("L").info("<%s> %s".formatted(sender.getName(), PlainTextComponentSerializer.plainText().serialize(message)));
    }


    public static void sendGlobalChat(Player sender, Component message) {
        Bukkit.broadcast(Component.text("<%s> ".formatted(sender.getName())).append(message));
    }
    public static String locationToString(Location loc) {
        if (loc == null) return null;
        Location l = loc.toBlockLocation();
        return "%.0f, %.0f, %.0f".formatted(l.x(), l.y(), l.z());
    }
    public static String boolToChar(boolean bool) {
        if (bool) return "T";
        else return "F";
    }
}
