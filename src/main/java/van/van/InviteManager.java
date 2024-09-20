package van.van;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class InviteManager {

    /* {Sender: [Target, RequestedTime] */
    public static Map<Player, Pair<String, Instant>> inviteRequests = new HashMap<>();
    private static BukkitTask scheduler;
    public static void start() {
        if (scheduler != null) {
            VAN.instance.getLogger().warning("Scheduler already started! restarting scheduler");
            scheduler.cancel();
        }

        scheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(VAN.instance, ()->{
            inviteRequests.entrySet().stream()
                    .filter(e -> e.getValue().getRight().isAfter(Instant.now()))
                    .forEach((e -> {
                        e.getKey().sendMessage(Component.text(e.getValue().getLeft()+"에게 보낸 초대 요청이 만료됐어요!")
                                .color(NamedTextColor.RED));
                        e.setValue(null);
                    })); //will work
        }, 0L, 20L);
    }
}
